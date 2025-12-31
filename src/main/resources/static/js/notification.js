// 페이지 로드 시 SSE 연결 시작
document.addEventListener('DOMContentLoaded', function() {
    if (typeof currentMemberId !== 'undefined' && currentMemberId) {

        if (typeof currentUserRole !== 'undefined' && currentUserRole === 'ROLE_ADMIN') {
            console.log("관리자 계정: 알림 기능을 활성화하지 않습니다.");
            return;
        }

        connectSSE(currentMemberId);
        initializeBadgeCount();
    }
});

function connectSSE(memberId) {
    const eventSource = new EventSource(`/api/notifications/subscribe/${memberId}?role=${currentUserRole}`);

    eventSource.addEventListener("notification", (event) => {
        updateBadgeCount(1);

        // 사이드바가 현재 열려있는 상태라면 목록을 즉시 새로고침
        const offcanvas = document.getElementById('offcanvasNotif');
        if (offcanvas && offcanvas.classList.contains('show')) {
            loadNotifications();
        }
    });

    eventSource.onerror = () => {
        console.log("SSE 연결 끊김, 재연결 시도 중...");
        eventSource.close();
        // 5초우 연결 재시도
        setTimeout(() => connectSSE(memberId), 5000);
    };
}

async function loadNotifications() {
    try {
        const response = await fetch(`/api/notifications/${currentMemberId}?role=${currentUserRole}`);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const notifications = await response.json();

        if (!Array.isArray(notifications)) {
            console.error("받은 데이터가 배열 형식이 아닙니다:", notifications);
            return;
        }

        const container = document.getElementById('notif-items');
        container.innerHTML = '';

        if (notifications.length === 0) {
            container.innerHTML = '<div class="p-5 text-center text-muted">알림이 없습니다.</div>';
            return;
        }

        notifications.forEach(n => {
            const div = document.createElement('div');
            const statusClass = n.read ? 'read' : 'unread';

            let targetUrl = null;
            let buttonText = "확인";

            switch (n.type) {
                // OWNER 케이스
                case 'RESERVATION_CREATE':
                case 'RESERVATION_UPDATE':
                case 'RESERVATION_CANCEL':
                case 'REVIEW_WRITE':
                    targetUrl = '/ownerPage';
                    buttonText = '사장페이지로';
                    break;

                // USER 케이스 (유저에게도 버튼을 보여줌)
                case 'RESERVATION_APPROVE':
                case 'RESERVATION_REJECT':
                case 'RESERVATION_CANCEL_REQUEST':
                    targetUrl = '/mypage';
                    buttonText = '예약확인';
                    break;

                default:
                    targetUrl = null; // NONE 이거나 매칭되지 않으면 버튼 없음
            }

            div.className = `p-4 border-bottom notif-item ${statusClass}`;

            const formattedDate = n.createdAt.replace('T', ' ').split('.')[0];
            const formattedMessage = n.message.replace('[', '<strong>[').replace(']', ']</strong><br>');

            div.innerHTML = `
                <div class="notif-content" onclick="markAsRead('${n.id}', this.parentElement)">
                    <p>${formattedMessage}</p>
                    <small class="text-muted">${formattedDate}</small>
                </div>
                ${targetUrl ? `
                    <div class="notif-action ms-2">
                        <button class="btn-move" onclick="handleMove('${n.id}', '${targetUrl}', this.parentElement.parentElement)">
                            ${buttonText}
                        </button>
                    </div>
                ` : ''}
            `;
            // div.onclick = () => markAsRead(n.id, div);
            container.appendChild(div);
        });
    } catch (error) {
        console.error("알림 로드 실패:", error);
    }
}

function updateBadgeCount(num) {
    const badge = document.getElementById('notif-badge');
    let currentCount = parseInt(badge.innerText) || 0;
    badge.innerText = currentCount + num;
    badge.style.display = 'block';
}

async function markAsRead(id, element) {
    if (element.classList.contains('read')) return;

    try {
        //currentUserRole을 문자열로 인식하게 함 <- IDE가 html의 요소나 jquery 객체로 인식하기 때문
        const roleStr = String(currentUserRole);
        //권한에 따라 owner, member 분기
        //왜 markAsRead만 분기를 나누는가 -> 다른 메소드들은 UserRole에 따라서 owner와 member를 구분함
        //markAsRead는 url에 쿼리파라미터로 memberId 또는 ownerId를 직접 보내기 때문
        const idParamName = (roleStr === 'ROLE_OWNER') ? 'ownerId' : 'memberId';
        const url = `/api/notifications/${id}/read?${idParamName}=${currentMemberId}&role=${currentUserRole}`;
        const response = await fetch(url, { method: 'PATCH' });

        if(response.ok) {
            element.classList.remove('unread');
            element.classList.add('read');
            updateBadgeCount(-1);
            // const badge = document.getElementById('notif-badge');
            // let currentCount = parseInt(badge.innerText) || 0;
            //
            // if (currentCount > 0) {
            //     currentCount--;
            //     badge.innerText = currentCount;
            // }
            //
            // if (currentCount <= 0) {
            //     badge.innerText = "0";
            //     badge.style.display = 'none';
            // }
        }
        else if(response.status === 403) {
            console.error("본인의 알림만 읽음 처리할 수 있습니다.");
        }
    } catch (e) {
        console.error("읽음 처리 실패", e);
    }
}

async function deleteAllNotifications(event) {
    event.stopPropagation();

    if (!confirm("모든 알림을 삭제하시겠습니까?")) {
        return;
    }

    try {
        const response = await fetch(`/api/notifications/all/${currentMemberId}?role=${currentUserRole}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            const items = document.querySelectorAll('.notif-item');

            items.forEach((item, index) => {
                setTimeout(() => {
                    item.classList.add('fade-out');
                }, index * 50); // 0.05초 간격으로 하나씩 사라짐
            });

            setTimeout(() => {
                const container = document.getElementById('notif-items');
                container.innerHTML = '<div class="p-5 text-center text-muted">알림이 없습니다.</div>';

                const badge = document.getElementById('notif-badge');
                badge.style.display = 'none';
                badge.innerText = "0";
            }, 500);
        } else {
            alert("삭제에 실패했습니다.");
        }
    } catch (error) {
        console.error("일괄 삭제 에러:", error);
    }
}

async function initializeBadgeCount() {
    try {
        const response = await fetch(`/api/notifications/${currentMemberId}?role=${currentUserRole}`);
        const notifications = await response.json();

        // 안 읽은(read가 false인) 알림의 개수만 필터링
        const unreadCount = notifications.filter(n => !n.read).length;

        if (unreadCount > 0) {
            const badge = document.getElementById('notif-badge');
            badge.innerText = unreadCount;
            badge.style.display = 'block';
        }
    } catch (error) {
        console.error("초기 배지 설정 실패:", error);
    }
}

async function handleMove(id, url, element) {
    await markAsRead(id, element); // 읽음 처리 먼저
    location.href = url; // 페이지 이동
}