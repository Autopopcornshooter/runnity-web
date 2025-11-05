// 랜덤 듀오 매칭
(function () {
  // ===== 설정 =====
  const API = {
    QUEUE: '/api/match/queue',
    STATE: '/api/match/state'
  };
  // localStorage keys
  const LS_FLAG = 'match.matching';
  const LS_PROG = 'match.match.progress';

  // 원형 게이지 길이
  const CIRC = 339.292;

  // ===== 유틸 =====
  const $ = (sel, root = document) => root.querySelector(sel);
  function getCsrfHeaders() {
    const t = $('meta[name="_csrf"]')?.content;
    const h = $('meta[name="_csrf_header"]')?.content;
    return (t && h) ? { [h]: t } : {};
  }

  // ===== 모달 DOM (없는 경우에만 생성) =====
  function ensureModal() {
    if ($('#matchModal')) return;

    const modal = document.createElement('div');
    modal.id = 'matchModal';
    modal.className = 'match-modal hidden';
    modal.innerHTML = `
      <div class="match-card">
        <div class="match-head">
          <strong>랜덤 채팅 매칭 중…</strong>
          <button type="button" id="matchCloseBtn" aria-label="close">✕</button>
        </div>

        <div class="ring-wrap">
          <svg class="ring" width="220" height="220" viewBox="0 0 120 120">
            <circle class="ring-bg" cx="60" cy="60" r="54"></circle>
            <circle class="ring-fg" id="ringProgress" cx="60" cy="60" r="54"></circle>
          </svg>
          <div class="percent" id="matchPercent">0%</div>
        </div>

        <div class="status" id="matchStatus">매칭 대기열에 등록했어요…</div>

        <div class="actions">
          <button type="button" id="matchStopBtn">매칭 취소</button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }

  // ===== 상태 =====
  let fakeTimer = null;
  let pollTimer = null;
  let progress = 0;
  let targetRoomId = null;

  // ===== View helpers =====
  function openModal() {
    ensureModal();
    const card = $('.match-card');
    $('#matchStatus').textContent = '매칭 대기열에 등록했어요…';
    $('#matchStopBtn').disabled = false;
    card?.classList.remove('success');
    setProgress(0);
    $('#matchModal').classList.remove('hidden');
  }
  function closeModal() {
    $('#matchModal')?.classList.add('hidden');
    clearInterval(fakeTimer); fakeTimer = null;
    clearInterval(pollTimer); pollTimer = null;
  }
  function setProgress(p) {
    progress = Math.max(0, Math.min(100, p));
    const ring = $('#ringProgress');
    const pct = $('#matchPercent');
    if (ring) ring.style.strokeDashoffset = CIRC * (1 - progress / 100);
    if (pct) pct.textContent = `${Math.floor(progress)}%`;
    // 시각 복구용으로 90%까지만 저장
    localStorage.setItem(LS_PROG, String(Math.min(90, progress)));
  }

  // ===== 서버 통신 =====
  async function startQueue() {
    const headers = getCsrfHeaders();
    const res = await fetch(API.QUEUE, { method: 'POST', headers });
    if (!res.ok) throw new Error('enqueue failed');
  }
  async function cancelQueue() {
    const headers = getCsrfHeaders();
    await fetch(API.QUEUE, { method: 'DELETE', headers }).catch(() => {});
  }
  async function fetchState() {
    const res = await fetch(API.STATE, { cache: 'no-store' });
    if (!res.ok) return null;
    return res.json();
  }

  // ===== 진행 애니메이션 =====
  function startFakeProgress() {
    const saved = Number(localStorage.getItem(LS_PROG) || '0');
    setProgress(Math.min(90, isNaN(saved) ? 0 : saved));
    fakeTimer = setInterval(() => {
      if (progress < 90) {
        // 남은 구간의 일부만 채우며 점점 느려지는 느낌
        setProgress(progress + Math.max(1, Math.floor((90 - progress) * 0.12)));
      }
    }, 450);
  }

  // ===== 상태 폴링 =====
  function startPolling() {
    clearInterval(pollTimer);
    pollTimer = setInterval(async () => {
      try {
        const data = await fetchState();
        if (!data) return;

        if (data.state === 'MATCHED') {
          targetRoomId = data.chatRoomId ?? data.room_id;
          onMatched();
        } else if (data.state === 'SEARCHING') {
          // 계속 대기
        } else {
          // 서버가 IDLE/UNKNOWN 등을 돌려준 경우
          persist(false);
          closeModal();
        }
      } catch (_) {}
    }, 900);
  }

  // ===== 매칭 완료 처리 =====
  function onMatched() {
    clearInterval(fakeTimer);
    clearInterval(pollTimer);
    setProgress(100);
    $('.match-card')?.classList.add('success');
    $('#matchStatus').textContent = '매칭 성공! 이동합니다…';
    $('#matchStopBtn').disabled = true;
    persist(false);

    setTimeout(() => {
      const to = targetRoomId ? `/chat-room/my-chat-list/${targetRoomId}` : '/chat-room/my-chat-list';
      window.location.href = to;
    }, 900);
  }

  // ===== 로컬 지속 =====
  function persist(on) {
    if (on) localStorage.setItem(LS_FLAG, 'true');
    else {
      localStorage.removeItem(LS_FLAG);
      localStorage.removeItem(LS_PROG);
    }
  }

  // ===== 공개 함수 =====
  async function startRandomMatching() {
    try {
      persist(true);
      openModal();
      await startQueue();     // 서버 대기열 등록
      startFakeProgress();    // 90%까지 진행바
      startPolling();         // 상태 폴링
    } catch (e) {
      persist(false);
      closeModal();
      alert('매칭 시작에 실패했습니다.');
    }
  }
  async function stopMatching() {
    $('#matchStopBtn')?.setAttribute('disabled', 'true');
    await cancelQueue();
    persist(false);
    closeModal();
  }

  // ===== 초기 바인딩 & 자동 복구 =====
  document.addEventListener('DOMContentLoaded', () => {
    ensureModal();

    // 버튼 연결 (메인 등 존재하는 페이지에서만)
    const btn = document.getElementById('duoMatchingBtn');
    if (btn) {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        startRandomMatching();
      });
    }

    // 모달 내 버튼
    $('#matchStopBtn')?.addEventListener('click', stopMatching);
    $('#matchCloseBtn')?.addEventListener('click', stopMatching);

    // 페이지 이동 후 자동 복구
    if (localStorage.getItem(LS_FLAG) === 'true') {
      openModal();
      startFakeProgress();
      startPolling();
    }
  });

  // 필요하면 전역으로 노출
  window.startRandomMatching = startRandomMatching;
  window.stopRandomMatching = stopMatching;
})();
