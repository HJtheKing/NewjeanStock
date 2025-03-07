// src/store/useDarkModeStore.js
import create from "zustand";

const useDarkModeStore = create((set) => ({
  darkMode: localStorage.getItem("theme") === "dark", // 초기값 설정
  toggleDarkMode: () => {
    set((state) => {
      const newDarkMode = !state.darkMode;
      // 다크 모드 상태에 따라 classList 및 localStorage 업데이트
      if (newDarkMode) {
        document.documentElement.classList.add("dark");
        localStorage.setItem("theme", "dark");
      } else {
        document.documentElement.classList.remove("dark");
        localStorage.setItem("theme", "light");
      }
      return { darkMode: newDarkMode };
    });
  },
}));

export default useDarkModeStore;
