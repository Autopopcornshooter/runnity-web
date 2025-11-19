const scCloseBtn = document.getElementById("joinCloseBtn");
const scModal = document.getElementById("scheduleJoinModal");

const scTitle = document.getElementById("joinTitle");
const scDate = document.getElementById("joinDate");
const scDesc = document.getElementById("joinDesc");
const scLocation = document.getElementById("joinLocation");
const scCreator = document.getElementById("creator");

const scDeleteBtn = document.getElementById("joinDeleteBtn");

const scJoinBtn = document.getElementById("joinYesBtn");
const scDeclineBtn = document.getElementById("joinNoBtn");

const scJoinCount = document.getElementById("joinYesCount");
const scDeclineCount = document.getElementById("joinNoCount");

const scParticipantStatus = document.getElementById("participantStatus");

let latestScheduleId = null
let myChatRoomMemberId = null;

let latestScheduleLat = null;
let latestScheduleLng = null;

//--모달 동작 관련--
async function openJoinModal() {
  await loadRecentSchedule();
  scModal.style.display = "flex";
//지도 세팅
  setTimeout(() => {
    initJoinMap();
  }, 100);
}

function closeJoinModal() {
  scModal.style.display = "none";
}

if (scModal) {
  scModal.addEventListener("click", (e) => {
    if (e.target === scModal) {
      closeJoinModal();
    }
  });
}

scCloseBtn.addEventListener("click", () => {
  closeJoinModal();
})

//--최근 일정 로드--

async function loadRecentSchedule() {
  const roomId = document.getElementById("currentRoomId").value;
  if (!roomId) {
    return;
  }
  try {
    const response = await fetch(`/chat-rooms/${roomId}/schedules/recent`, {
      method: "GET"
    });
    if (!response.ok) {
      const message = await response.text();
      throw new Error("일정 불러오기 실패 : " + message);
    }
    // TODO
    const data = await response.json();
    fillScheduleData(data);
    fillRecentScheduleBar(data);
    //scOpenBtn.style.display = "inline-block";

  } catch (err) {
    console.log("일정 없음 : " + err);
    fillRecentScheduleBar(null);
    //scOpenBtn.style.display = "none";
  }
}

window.addEventListener('room:active', (e) => {
  loadRecentSchedule();
});

//--일정 데이터 삽입--

function fillScheduleData(data) {
  latestScheduleId = data.scheduleId;
  myChatRoomMemberId = data.memberId;

  scTitle.textContent = data.title;
  scDesc.textContent = data.detail;
  scLocation.textContent = data.location ?? '-';

  // 지도용 좌표 저장
  scLocation.dataset.lat = data.lat;
  scLocation.dataset.lng = data.lng;
  latestScheduleLat = data.lat;
  latestScheduleLng = data.lng;

  const dateObj = new Date(data.startAt);
  const now = new Date();
  const diffHour = (now - dateObj) / (1000 * 60 * 60);

  const dateStr = data.startAt.replace("T", " ").slice(0, 16);
  scDate.textContent = `일시: ${dateStr}`;

  scCreator.value = data.scheduleCreatorId;

  scJoinCount.textContent = data.yesCount ?? 0;
  scDeclineCount.textContent = data.noCount ?? 0;

  // ====== 이미 지난 일정 처리 ======
  if (now > dateObj) {
    // 일정 종료 후 5시간 이하
    if (diffHour <= 5) {
      scDate.style.color = "red";
      scDate.textContent += "  (일정 시간이 지났습니다)";

      // 버튼 비활성화
      scJoinBtn.disabled = true;
      scDeclineBtn.disabled = true;
      scJoinBtn.classList.add("disabled");
      scDeclineBtn.classList.add("disabled");
    } else {
      // 5시간보다 더 지났으면 UI에서 완전히 숨김
      hideExpiredSchedule();
      return;
    }
  } else {
    // 정상 일정
    scDate.style.color = "";
    scJoinBtn.disabled = false;
    scDeclineBtn.disabled = false;
    scJoinBtn.classList.remove("disabled");
    scDeclineBtn.classList.remove("disabled");
  }
  //삭제버튼 활성/비활성화
  if (data.isCreator) {
    scDeleteBtn.style.display = "block";
  } else {
    scDeleteBtn.style.display = "none";
  }
  fillJoinStatus(data.participantStatus);
}

function fillJoinStatus(status) {
  console.log(status);
  switch (status) {
    case "JOINED":
      toggleSelect(scJoinBtn);
      break;
    case "DECLINED":
      toggleSelect(scDeclineBtn);
      break;
    case "PENDING":
      toggleSelect(null);
      break;
  }
}

//--만료된 일정 숨김--
function hideExpiredSchedule() {

  const bar = document.getElementById("recentScheduleBar");
  if (bar) {
    bar.style.display = "none";
  }

  closeJoinModal();

  latestScheduleId = null;
}

//참가/불참 버튼 라디오 효과 추가
function toggleSelect(selectedBtn) {
  scJoinBtn.classList.remove("active");
  scDeclineBtn.classList.remove("active");
  if (selectedBtn !== null) {
    selectedBtn.classList.add("active");
  }
}

scJoinBtn.addEventListener("click", async (e) => {
  toggleSelect(scJoinBtn);
  await updateParticipant("JOINED");
});
scDeclineBtn.addEventListener("click", async (e) => {
  toggleSelect(scDeclineBtn);
  await updateParticipant("DECLINED");
});

async function updateParticipant(status) {
  if (!latestScheduleId || !myChatRoomMemberId) {
    console.error(
        "latestScheduleId: " + latestScheduleId + " myChatRoomMemberId: "
        + myChatRoomMemberId);
    return;
  }
  const payload = {
    participantStatus: status,
    scheduleId: latestScheduleId,
    memberId: myChatRoomMemberId
  };
  const tokenMeta = document.querySelector('meta[name="_csrf"]');
  const headerMeta = document.querySelector('meta[name="_csrf_header"]');

  const headers = {
    "Content-Type": "application/json"
  };

  if (tokenMeta && headerMeta) {
    headers[headerMeta.content] = tokenMeta.content;
  }

  const response = await fetch("/chat-rooms/joinSelect", {
    method: "PUT",
    headers,
    body: JSON.stringify(payload)
  });

  if (!response.ok) {

    throw new Error("저장 실패");
  }
  console.log("저장 성공");
  //TODO
  const data = await response.json();

  fillJoinStatus(data.participantStatus);
  scJoinCount.textContent = data.yesCount;
  scDeclineCount.textContent = data.noCount;

}

function fillRecentScheduleBar(data) {
  const bar = document.getElementById("recentScheduleBar");
  const text = document.getElementById("recentScheduleText");

  if (!bar || !text) {
    return;
  }

  if (!data) {
    bar.style.display = "none";
    return;
  }

  const start = new Date(data.startAt);
  const now = new Date();
  const diffHour = (now - start) / (1000 * 60 * 60);

  let displayText = "";
  let color = "";
  let clickable = true;

  // 🔥 완전 종료된 일정 (5시간 이상 지남)
  if (diffHour > 5) {
    bar.style.display = "none";
    return;
  }

  // 🔥 종료 되었지만 5시간 이내
  if (now > start) {
    displayText = `⛔ 일정 종료됨 · ${data.title}`;
    color = "red";
    clickable = false;
  }
  // 🔥 정상 일정
  else {
    const formattedTime = formatStartTime(data.startAt);
    displayText = `🕒 ${formattedTime} · ${data.title}`;
    color = "";
  }

  // bar 표시
  text.textContent = displayText;
  text.style.color = color;
  bar.style.display = "inline-flex";

  // 클릭 시 모달 열기
  bar.onclick = () => {
    fillScheduleData(data);   // 기존 모달 데이터 작성
    openJoinModal();          // 모달 오픈
  };
}

function formatStartTime(startAt) {
  const start = new Date(startAt);
  const now = new Date();

  const diffMs = start - now;
  const diffMin = Math.floor(diffMs / 60000);
  const diffHour = Math.floor(diffMin / 60);

  if (diffMin < 1) {
    return "곧 시작";
  }
  if (diffMin < 60) {
    return `${diffMin}분 후 시작`;
  }
  if (diffHour < 24) {
    return `${diffHour}시간 후 시작`;
  }

  const isTomorrow = start.getDate() === now.getDate() + 1;
  if (isTomorrow) {
    return `내일 ${start.getHours()}시 시작`;
  }

  return `${start.getMonth() + 1}월 ${start.getDate()}일 ${start.getHours()}시`;
}

let joinMap = null;
let joinMarker = null;

function initJoinMap() {

  const mapDiv = document.getElementById("scheduleViewMap");
  if (!mapDiv) {
    return;
  }

  const lat = Number(scLocation.dataset.lat);
  const lng = Number(scLocation.dataset.lng);
  console.log("lat:", lat, "lng:", lng);
  if (!lat || !lng || isNaN(lat) || isNaN(lng)) {
    mapDiv.innerHTML = "위치 정보 없음";
    return;
  }

  if (!joinMap) {
    joinMap = new naver.maps.Map('scheduleViewMap', {
      center: new naver.maps.LatLng(lat, lng),
      zoom: 15,
      draggable: true,
      pinchZoom: true,
      disableDoubleClickZoom: false,
      scrollWheel: true,
      keyboardShortcuts: true
    });

    joinMarker = new naver.maps.Marker({
      position: new naver.maps.LatLng(lat, lng),
      map: joinMap,
      clickable: false,
      draggable: false
    });
  } else {
    const pos = new naver.maps.LatLng(lat, lng);
    joinMap.setCenter(pos);
    joinMarker.setPosition(pos);
  }
}