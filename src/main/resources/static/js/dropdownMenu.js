const wrap = document.querySelector('.menu-wrap');
const btn = wrap.querySelector('.menu-btn');
const menu = wrap.querySelector('.menu');
const submenuToggle = wrap.querySelector('.submenu-toggle');
const submenu = wrap.querySelector('.submenu');
const levelButtons = wrap.querySelectorAll('.level-opt');

// 메뉴 토글
btn.addEventListener('click', () => {
  const show = !menu.classList.contains('show');
  menu.classList.toggle('show', show);
  btn.setAttribute('aria-expanded', String(show));
  if (!show) {
    submenu.classList.remove('show');
  }
});

// 하위 메뉴 토글
submenuToggle.addEventListener('click', (e) => {
  e.stopPropagation();
  const show = !submenu.classList.contains('show');
  submenu.classList.toggle('show', show);
});

// 외부 클릭 시 닫기
document.addEventListener('click', (e) => {
  if (!wrap.contains(e.target)) {
    menu.classList.remove('show');
    submenu.classList.remove('show');
  }
});

// 선택 → 즉시 POST 전송
async function postRunnerLevel(runnerLevel) {
  const endpoint = '/userInfo/runner-level/update'; // 실제 API 주소에 맞게 변경
  const csrf = document.querySelector('meta[name="_csrf"]')?.getAttribute(
      'content');
  const csrfHeader = document.querySelector(
      'meta[name="_csrf_header"]')?.getAttribute('content');

  const headers = {'Content-Type': 'application/json'};
  if (csrf && csrfHeader) {
    headers[csrfHeader] = csrf;
  }

  await fetch(endpoint, {
    method: 'POST',
    headers,
    body: JSON.stringify({runnerLevel: runnerLevel})
  });

  alert("러너 레벨 변경 완료: " + runnerLevel);
}

// 클릭 시 즉시 반영
levelButtons.forEach(btn => {
  btn.addEventListener('click', () => postRunnerLevel(btn.dataset.runnerLevel));
});
