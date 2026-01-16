// Plotly 차트 옵션
const config = {
    responsive: true,
    displayModeBar: false,
    scrollZoom: false,
}

// 공통 로딩 표시 함수
function showLoading(elementId) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerHTML = `
            <div class="d-flex justify-content-center align-items-center" style="height: 100%;">
                <div class="spinner-border text-primary" role="status"></div>
                <span class="ms-2">데이터 분석 중...</span>
            </div>`;
    }
}

// 슬라이드 1 : 인기 카테고리 차트
async function drawHeroCategoryChart() {
    const chartDiv = document.getElementById('categoryDistributionChart');
    if (!chartDiv) return;

    try {
        showLoading('categoryDistributionChart');
        const res = await fetch('/api/index/charts/category-ranking');
        const resJson = await res.json();

        // 파이썬 에러 체크
        if (resJson.error || !resJson.data) {
            throw new Error(resJson.error || "No Data");
        }

        chartDiv.innerHTML = '';
        await Plotly.newPlot(chartDiv, resJson.data, resJson.layout, config);

        // 클릭 이벤트 (카테고리 이동)
        chartDiv.on('plotly_click', function(data) {
            const category = data.points[0].label;
            location.href = `/diner/list?category=${encodeURIComponent(category)}`;
        });
    }
    catch(e) {
        console.error("카테고리 차트 로드 실패:", e);
        chartDiv.innerHTML = '<div class="text-center text-muted pt-5">인기 카테고리 통계가 부족합니다.</div>';
    }
}

// 슬라이드 3 : 오늘의 식당 키워드 차트
async function loadFeaturedKeywords() {
    const chartDiv = document.getElementById('keywordAnalysisChart');
    if (!chartDiv) return;

    try {
        showLoading('keywordAnalysisChart');
        const res = await fetch('/api/index/charts/featured-keywords');
        const resJson = await res.json();

        if (resJson.error || !resJson.data) {
            console.warn("Python 에러 내용:", resJson.error);
            chartDiv.innerHTML = '<div class="text-center text-muted pt-5">분석할 리뷰 데이터가 부족합니다.</div>';
            return;
        }

        chartDiv.innerHTML = '';
        await Plotly.newPlot(chartDiv, resJson.data, resJson.layout, config);
    }
    catch(e) {
        console.error("키워드 차트 로드 실패:", e);
        chartDiv.innerHTML = '<div class="text-center text-muted pt-5">키워드 데이터를 불러올 수 없습니다.</div>';
    }
}

document.addEventListener('DOMContentLoaded', () => {
    // 순차적 실행이 아닌 개별 실행
    drawHeroCategoryChart();
    loadFeaturedKeywords();

    // 슬라이드 전환 시 차트 크기 깨짐 방지
    const heroCarousel = document.getElementById('heroCarousel');
    if (heroCarousel) {
        heroCarousel.addEventListener('slid.bs.carousel', () => {
            // 차트가 생성된 후에만 리사이즈 시도
            const charts = ['categoryDistributionChart', 'keywordAnalysisChart'];
            charts.forEach(id => {
                const el = document.getElementById(id);
                // Plotly 클래스가 붙어있는지 확인 후 리사이즈
                if (el && el.classList.contains('js-plotly-plot')) {
                    Plotly.Plots.resize(el);
                }
            });
        });
    }
});