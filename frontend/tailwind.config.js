/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class", // 다크 모드 설정 (class 방식)
  content: ["./index.html", "./src/**/*.{js,ts,jsx,tsx}"],
  theme: {
    extend: {
      colors: {
        yellow1: "#FFD700",
        yellow2: "#FFE86C",
        yellow3: "#FCF5C7",
        // 추가적인 색상을 여기에서 정의할 수 있습니다.
        // 예: dark 모드 색상 추가
        grayDark: "#1F1F1F", // 다크 모드용 어두운 회색
        // 다크 모드 전용 색상 추가
        grayLight: "#E5E5E5", // 라이트 모드용 연한 회색
      },
    },
  },
  plugins: [],
};
