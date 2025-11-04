async function checkAuth() {
  const token = localStorage.getItem("access_token");

  if (!token) {
    console.warn("No Token Detected");
    window.location.href = "/signIn.html";
    return;
  }

  try {
    const response = await fetch("/api/auth/test", {
      method: "GET",
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      },
      credentials: "include"
    });

    if (!response.ok) {
      console.warn("Token Expired");
      window.location.href = "/signIn.html";
    } else {
      console.log("✅ 인증 통과: 페이지 접근 허용");
    }
  } catch (err) {
    console.error("인증 요청 실패:", err);
    window.location.href = "/signIn.html";
  }
}

// 페이지가 로드될 때 자동으로 인증 검사
window.addEventListener("DOMContentLoaded", checkAuth);