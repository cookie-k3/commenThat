import "./App.css";
import Main from "./pages/Main";
import Home from "./pages/Home";
import About from "./pages/About";
import AnalysisCenter from "./pages/AnalysisCenter";
import Signup from "./pages/Signup";
import Login from "./pages/Login";
import Notfound from "./pages/Notfound";
import Subscriber from "./pages/Subscriber";
import View from "./pages/View";
import Category from "./pages/Category";
import Sentiment from "./pages/Sentiment";
import ContentRecommend from "./pages/ContentRecommend";
import CategoryDetail from "./pages/CategoryDetail";
import { Routes, Route } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";

function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<Main />} />
        <Route path="/home" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/analysis_center" element={<AnalysisCenter />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/login" element={<Login />} />
        <Route path="/subscribers" element={<Subscriber />} />
        <Route path="/views" element={<View />} />
        <Route path="/category" element={<Category />} />
        <Route path="/sentiment" element={<Sentiment />} />
        <Route path="/recommend" element={<ContentRecommend />} />
        <Route path="*" element={<Notfound />} />
        <Route path="/category/detail" element={<CategoryDetail />} />
      </Routes>
    </AuthProvider>
  );
}

export default App;
