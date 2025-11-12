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

// 드롭 다운 나의 채팅방 실시간 메세지 알림
(() => {
  let stomp = null;
  let sub = null;
  let ready = null;
  let activeRoomId = null; // 현재 활성화된 채팅방에서의 메세지는 count 무시
  const userIdMeta = document.querySelector('meta[name="current-user-id"]');
  const currentUserId = userIdMeta ? Number(userIdMeta.content) : null;

  window.addEventListener('room:active', (e) => {
    activeRoomId = e?.detail?.roomId ?? null;
  });

  function getBadge() {
    const link = document.querySelector('.menu a[href="/chat-room/my-chat-list"]');
    if (!link) return null;
    let badge = link.querySelector('.menu-badge');
    if (!badge) {
      badge = document.createElement('span');
      badge.className = 'menu-badge';
      link.prepend(badge);
    }
    return badge;
  }

  function setBadgeCount(n) {
    const badge = getBadge();
    if (!badge) return;
    const count = Number(n) || 0;
    if (count > 0) {
      badge.textContent = count > 99 ? '99+' : String(count);
      badge.hidden = false;

      // 새로운 알림 있을 때 효과
      badge.classList.remove('flash');
      void badge.offsetWidth;
      badge.classList.add('flash');
    } else {
      badge.textContent = '';
      badge.hidden = true;
    }
  }

  async function fetchUnreadTotal() {
    try {
      const res = await fetch('/api/chat-rooms/unread-counts', { cache: 'no-store' });
      if (!res.ok) return 0;
      const data = await res.json();
      let total = 0;
      for (const key in data) {
        if (Object.prototype.hasOwnProperty.call(data, key)) {
          total += Number(data[key]) || 0;
        }
      }
      return total;
    } catch {
      return 0;
    }
  }

  function ensureConnection() {
    if (ready) return ready;
    ready = new Promise((resolve) => {
      const sock = new SockJS('/ws-chat');
      stomp = Stomp.over(sock);
      stomp.connect({}, () => resolve());
    });
    return ready;
  }

  function subscribeNotify() {
    if (!stomp || sub) return;
    sub = stomp.subscribe('/user/queue/notify', async (frame) => {
      try {
        const payload = JSON.parse(frame.body || '{}');
        if (payload.type !== 'NEW_MESSAGE') return;

        const senderId = Number(payload.senderId);
        const roomId = Number(payload.roomId ?? payload.chatRoomId);

        if (currentUserId && senderId === currentUserId) return;
        if (activeRoomId && roomId === Number(activeRoomId)) return;

        const current = await fetchUnreadTotal();
        setBadgeCount(current);
      } catch (err) {
        console.warn('배지 실시간 업데이트 에러', err);
      }
    });
  }

  async function initBadgeRealtime() {
    setBadgeCount(await fetchUnreadTotal());
    await ensureConnection();
    subscribeNotify();

    window.addEventListener('unread:refresh', async () => {
      setBadgeCount(await fetchUnreadTotal());
    });
  }

  document.addEventListener('DOMContentLoaded', () => {
    if (getBadge()) {
      initBadgeRealtime();
    }
  });
})();
