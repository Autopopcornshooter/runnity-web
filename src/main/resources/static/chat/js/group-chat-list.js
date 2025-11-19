let previewRoomId = null;

async function openPreview(el) {
  const id = el.dataset.id;
  const title = el.dataset.title || '이름 없는 채팅방';
  const desc = el.dataset.desc || '설명이 없습니다.';
  const img = el.dataset.img || '/images/image-upload.png';
  const owner = el.dataset.owner || '알 수 없음';

  previewRoomId = Number(id);

  document.getElementById('pvImg').src = img;
  document.getElementById('pvImg').alt = title;
  document.getElementById('pvTitleText').textContent = title;
  document.getElementById('pvDesc').textContent = desc;
  document.getElementById('pvOwner').textContent = owner;
  const checkJoin = document.getElementById("pvLabel");

  const joinBtn = document.getElementById('pvJoinBtn');
  joinBtn.textContent = '참여하기';
  joinBtn.disabled = false;
  joinBtn.classList.remove('disabled');
  joinBtn.onclick = () => joinAndGo(previewRoomId);

  try {
    const res = await fetch(`/api/chats/${id}/joined`,
        {
          method: 'GET'
        }
    );

    if (res.ok) {
      const { joined } = await res.json();
      if (joined) {
        checkJoin.style.display = "inline-block";
        checkJoin.textContent = '이미 참여한 채팅방';
        checkJoin.style.background = '#cbd5e1';
        joinBtn.style.background = '#cbd5e1';
        joinBtn.style.border = '1px solid #cbd5e1';
        joinBtn.textContent = '채팅방으로 가기';
      }
    }

  } catch (err) {
    console.warn('참여 여부 확인 실패', err);
  }

  const modal = document.getElementById('roomPreview');
  modal.classList.remove('hidden');
  modal.setAttribute('aria-hidden', 'false');
}

async function joinAndGo(chatRoomId) {
  try {
    const token  = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;

    const res = await fetch(`/api/chats/${chatRoomId}/join`, {
      method: 'POST',
      headers: {[header]: token}
    });

    if (res.status === 201 || res.ok) {
      // 원하는 최종 경로로 이동
      window.location.href = `/chat-room/my-chat-list/${chatRoomId}`;
      return;
    }

    if (res.status === 401) {
      alert('로그인이 필요합니다');
      location.href = '/api/auth/signIn';
      return;
    }
    if (res.status === 403) {
      alert('권한이 없습니다');
      return;
    }

    const text = await res.text();
    alert(`JOIN 실패: ${text}`);
  } catch (e) {
    console.error(e);
    alert('네트워크 오류가 발생했어요.');
  }
}

function closePreview() {
  const modal = document.getElementById('roomPreview');
  modal.classList.add('hidden');
  modal.setAttribute('aria-hidden', 'true');
  previewRoomId = null;
}

document.addEventListener('click', (e) => {
  if (e.target.matches('#roomPreview [data-close="true"]')) {
    closePreview();
  }
});

document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') closePreview();
});

// 이미 가지고 오는 데이트들이 존재해서 그 데이터들을 활용하면 DOM에서 텍스트를 다시 파싱할 필요 X
document.addEventListener("DOMContentLoaded", function () {

  const searchInput = document.getElementById("searchInput");

  if (searchInput) {
    searchInput.addEventListener("keyup", function () {

      const keyword = searchInput.value.toLowerCase().trim();

      const roomCards = document.querySelectorAll(".chat-room-card");

      roomCards.forEach((card) => {
        const title = card.getAttribute("data-title")?.toLowerCase() || "";
        const region = card.querySelector(".chat-room-meta span span")?.innerText.toLowerCase() || "";

        const match =
            title.includes(keyword) ||
            region.includes(keyword);

        // 매칭되면 보이고, 아니면 숨기기
        if (match) {
          card.style.display = "flex";
        } else {
          card.style.display = "none";
        }
      });
    });
  }
});