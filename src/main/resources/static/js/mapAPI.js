// 1. 기본 지도 생성
var map = new naver.maps.Map('map', {
  center: new naver.maps.LatLng(37.5665, 126.9780), // 서울시청
  zoom: 14
});

var latlng = null;

var marker = new naver.maps.Marker({
  map: map
});

var infoDiv = document.getElementById("info");

window.onload = function () {
  latlng = new naver.maps.LatLng(window.currentLat, window.currentLng)
  marker.setPosition(latlng);
  checkAddress(window.currentAddress);
  map.setCenter(latlng);
  console.log(latlng._lat);
  console.log(latlng._lng);
}

// 2. 클릭 시 좌표 및 주소 표시
naver.maps.Event.addListener(map, 'click', function (e) {
  latlng = e.coord;

  marker.setPosition(latlng);

  naver.maps.Service.reverseGeocode({
    coords: latlng,
    orders: [naver.maps.Service.OrderType.ADDR,
      naver.maps.Service.OrderType.ROAD_ADDR].join(',')
  }, function (status, response) {
    if (status !== naver.maps.Service.Status.OK) {
      return alert("주소를 찾을 수 없습니다.");
    }
    var address = response.v2.address.roadAddress
        || response.v2.address.jibunAddress;
    // infoDiv.innerHTML = `좌표: ${latlng.y}, ${latlng.x}<br>주소: ${address}`;
    checkAddress(`${address}`);
  });
});

// 3. 검색창 기능
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput")
searchBtn.addEventListener("click", searchRegion);
searchInput.addEventListener("keydown", function (e) {
  if (e.key === "Enter") {
    e.preventDefault();
    searchRegion();
  }
});

function searchRegion() {
  var keyword = searchInput.value;
  naver.maps.Service.geocode({query: keyword}, function (status, response) {
    if (status !== naver.maps.Service.Status.OK) {
      return alert("검색 실패");
    }
    var item = response.v2.addresses[0];
    if (!item) {
      return alert("지역이 존재하지 않습니다");
    }
    latlng = new naver.maps.LatLng(item.y, item.x);
    map.setCenter(latlng);
    marker.setPosition(latlng);
    checkAddress(`${item.roadAddress || item.jibunAddress}`);
  });
}

// 4. 현재 위치 버튼
document.getElementById("currentBtn").onclick = function () {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(function (pos) {
      latlng = new naver.maps.LatLng(pos.coords.latitude,
          pos.coords.longitude);
      map.setCenter(latlng);
      marker.setPosition(latlng);

      naver.maps.Service.reverseGeocode({coords: latlng},
          function (status, response) {
            if (status !== naver.maps.Service.Status.OK) {
              return;
            }
            var address = response.v2.address.roadAddress
                || response.v2.address.jibunAddress;
            checkAddress(`${address}`);
          });
    });
  } else {
    alert("현재 위치를 지원하지 않습니다.");
  }
};

function checkAddress(address) {
  infoDiv.innerHTML = address;
  document.getElementById("address").value = address;
  document.getElementById("lat").value = latlng._lat;
  document.getElementById("lng").value = latlng._lng;
  console.log(address);
  console.log(latlng._lat);
  console.log(latlng._lng);
}