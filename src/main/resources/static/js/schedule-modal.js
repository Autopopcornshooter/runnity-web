const modal = $("#scheduleModal");
const openBtn = $("#openScheduleModalBtn");
const closeBtn = $("#scheduleModalCloseBtn");
const cancelBtn = $("#scheduleCancelBtn");
const submitBtn = $("#scheduleSubmitBtn");

let roomIdInput = null;
const titleInput = $("#scheduleTitle");
const dtInput = $("#scheduleDateTime");
const detailInput = $("#scheduleDetail");
const regionInput = $("#scheduleRegionId");

function openModal() {
  roomIdInput = document.getElementById("currentRoomId");
  console.log("일정 생성 모달");
  if (!roomIdInput) {
    alert("일정을 생성할 채팅방을 먼저 선택하세요.");
    return;
  }

  console.log("currentRoomId: " + roomIdInput.value);

  titleInput.value = "";
  dtInput.value = "";
  detailInput.value = "";

  modal.style.display = "flex";
}

function closeModal() {
  modal.style.display = "none";
}

async function submitSchedule() {
  const roomId = Number(roomIdInput.value);

  if (!roomId) {
    alert("채팅방 정보가 없습니다. 다시 시도해주세요.");
    return;
  }

  const title = (titleInput.value || "").trim();
  const detail = (detailInput.value || "").trim();
  const startAt = dtInput.value;  // "2025-11-15T14:30" 같은 문자열
  const regionId = regionInput.value ? Number(regionInput.value) : null;

  if (!title) {
    alert("제목을 입력해주세요.");
    titleInput.focus();
    return;
  }
  if (!startAt) {
    alert("모임 시간을 선택해주세요.");
    dtInput.focus();
    return;
  }

  // CSRF 헤더 (스프링 시큐리티 사용 중일 때)
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');
  const headers = {
    "Content-Type": "application/json"
  };
  if (tokenMeta && headerMeta) {
    headers[headerMeta.content] = tokenMeta.content;
  }

  const payload = {
    title,
    detail,
    startAt,
    regionId,
    roomId // 서버 DTO에서 roomId 필드 받고 있으면 같이 전달
  };

  try {
    const res = await fetch(`/chat-rooms/${roomId}/create-schedule`, {
      method: "POST",
      headers,
      body: JSON.stringify(payload)
    });

    if (!res.ok) {
      const msg = await res.text();
      throw new Error(msg || "일정 생성 실패");
    }

    alert("일정이 생성되었습니다.");
    closeModal();
    window.location.href = `/chat-room/my-chat-list/${roomId}`;

  } catch (err) {
    console.error("일정 생성 실패:", err);
    alert("일정 생성에 실패했습니다.\n" + (err.message || ""));
  }
}

// 이벤트 바인딩

if (openBtn) {
  openBtn.addEventListener("click", openModal);
}

window.addEventListener('room:active', (e) => {
  const roomId = e.detail.roomId;
  console.log("채팅방 활성화 이벤트")
  if (roomId) {
    openBtn.style.display = "inline-block";
  } else {
    openBtn.style.display = "none";
  }
});

if (closeBtn) {
  closeBtn.addEventListener("click", closeModal);
}
if (cancelBtn) {
  cancelBtn.addEventListener("click", closeModal);
}
if (submitBtn) {
  submitBtn.addEventListener("click", submitSchedule);
}

// 배경 클릭 시 닫기 (선택사항)
if (modal) {
  modal.addEventListener("click", (e) => {
    if (e.target === modal) {
      closeModal();
    }
  });
}




