// src/context/AuthContext.jsx
import React, { createContext, useContext, useState } from "react";

// 1. Context 생성
const AuthContext = createContext();

// 2. Provider 컴포넌트
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);

  const login = (userInfo) => {
    setUser(userInfo); // userInfo = { loginId: 'user1' } 이런 식으로
  };

  const logout = () => {
    setUser(null);
    // navigate는 여기서 하지 말고 컴포넌트에서 해줄 것!
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

// 3. useContext 훅 export
export const useAuth = () => useContext(AuthContext);
