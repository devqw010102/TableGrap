// 페이지 로드 시 SSE 연결 시작
document.addEventListener('DOMContentLoaded', function() {
    if (typeof currentMemberId !== 'undefined' && currentMemberId) {
        connectSSE(currentMemberId);
        initializeBadgeCount()
    }
});

function connectSSE(memberId) {
    const eventSource = new EventSource('/api/notifications/subscribe/' + memberId);

    eventSource.addEventListener("notification", (event) => {
        updateBadgeCount(1);
    });

    eventSource.onerror = () => {
        console.log("SSE 연결 끊김, 재연결 시도 중...");
        eventSource.close();
    };
}

async function loadNotifications() {
    try {
        const response = await fetch('/api/notifications/' + currentMemberId);

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
            container.innerHTML = '<li class="dropdown-item text-center text-muted">알림이 없습니다.</li>';
            return;
        }

        notifications.forEach(n => {
            const li = document.createElement('li');
            const statusClass = n.read ? 'read' : 'unread';

            li.className = `dropdown-item p-3 border-bottom notif-item ${statusClass}`;
            li.innerHTML = `
                <div style="white-space: normal; cursor: pointer;">
                    <p class="mb-1" style="font-size: 0.9rem;">${n.message}</p>
                    <small class="text-muted">${new Date(n.createdAt).toLocaleString()}</small>
                </div>
            `;
            li.onclick = () => markAsRead(n.id, li);
            container.appendChild(li);
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
        const response = await fetch(`/api/notifications/${id}/read`, { method: 'PATCH' });

        if(response.ok) {
            element.classList.remove('unread');
            element.classList.add('read');

            const badge = document.getElementById('notif-badge');
            let currentCount = parseInt(badge.innerText) || 0;

            if (currentCount > 0) {
                currentCount--;
                badge.innerText = currentCount;
            }

            if (currentCount <= 0) {
                badge.innerText = "0";
                badge.style.display = 'none';
            }
        }
        element.style.backgroundColor = 'transparent';
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
        const response = await fetch(`/api/notifications/all/${currentMemberId}`, {
            method: 'DELETE'
        });

        if (response.ok) {
            alert("모든 알림이 삭제되었습니다.");

            const container = document.getElementById('notif-items');
            container.innerHTML = '<li class="dropdown-item text-center text-muted">알림이 없습니다.</li>';

            const badge = document.getElementById('notif-badge');
            badge.style.display = 'none';
            badge.innerText = "0";
        } else {
            alert("삭제에 실패했습니다.");
        }
    } catch (error) {
        console.error("일괄 삭제 에러:", error);
    }
}

async function initializeBadgeCount() {
    try {
        const response = await fetch('/api/notifications/' + currentMemberId);
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