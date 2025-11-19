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
  //지도 초기화
  document.getElementById("scheduleModal").style.display = "flex";
  setTimeout(() => initCreateMap(), 200);
  //
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

  const regionId = document.getElementById("scheduleRegionId").value;
  const lat = document.getElementById("scheduleLat").value;
  const lng = document.getElementById("scheduleLng").value;
  const address = document.getElementById("scheduleAddress").value;

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
    roomId, // 서버 DTO에서 roomId 필드 받고 있으면 같이 전달
    regionId: regionId ? Number(regionId) : null,
    lat: lat ? Number(lat) : null,
    lng: lng ? Number(lng) : null,
    address
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

window.addEventListener('room:active', (e) => {
  const roomId = e.detail.roomId;
  console.log("채팅방 활성화 이벤트")
  if (roomId) {
    openBtn.style.display = "inline-block";
  } else {
    openBtn.style.display = "none";
  }
});

if (openBtn) {
  openBtn.addEventListener("click", openModal);
}

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

let createMap, createMarker;

function initCreateMap() {
  const defaultCenter = new naver.maps.LatLng(37.5665, 126.9780); // 서울 기본 위치

  createMap = new naver.maps.Map('scheduleCreateMap', {
    center: defaultCenter,
    zoom: 14
  });

  createMarker = new naver.maps.Marker({
    position: defaultCenter,
    map: createMap,
    draggable: true
  });

  naver.maps.Event.addListener(createMap, 'click', function (e) {
    createMarker.setPosition(e.coord);
    updateCreateMapPosition(e.coord);
  });

  naver.maps.Event.addListener(createMarker, 'dragend', function (e) {
    updateCreateMapPosition(e.coord);
  });

  updateCreateMapPosition(defaultCenter);
}

function updateCreateMapPosition(latlng) {
  document.getElementById("scheduleLat").value = latlng.lat();
  document.getElementById("scheduleLng").value = latlng.lng();

  naver.maps.Service.reverseGeocode({
    coords: latlng,
    orders: naver.maps.Service.OrderType.ADDR
  }, function (status, response) {
    if (status !== naver.maps.Service.Status.OK) {
      return;
    }

    const address = response.v2.address.roadAddress
        || response.v2.address.jibunAddress;
    document.getElementById("scheduleAddress").value = address;
  });
}


