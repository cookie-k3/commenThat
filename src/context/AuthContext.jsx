// src/context/AuthContext.jsx
import React, { createContext, useContext, useState, useEffect } from "react";

// 1. Context 생성
const AuthContext = createContext();

// 2. Provider 컴포넌트
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  const login = (userInfo) => {
    console.log("setUser로 저장하는 userInfo:", userInfo);
    setUser(userInfo); // userInfo = { loginId: 'user1' } 이런 식으로
    localStorage.setItem("user", JSON.stringify(userInfo)); //페이지 새로고침해도 로그인 정보가 저장되도록 로컬스토리지에 저장
  };

  const logout = () => {
    setUser(null); // 로그인된 사용자 정보 초기화
    // navigate는 여기서 하지 말고 컴포넌트에서 해줄 것!
    localStorage.removeItem("user"); //로그아웃 시 localStorage에서 사용자 정보 제거
  };

  //새로고침 시 복원
  useEffect(() => {
    const storedUser = localStorage.getItem("user");
    if (storedUser) {
      setUser(JSON.parse(storedUser)); // 초기 복원
    }
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// 3. useContext 훅 export
export const useAuth = () => useContext(AuthContext);
