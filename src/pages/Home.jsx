import { useParams, useNavigate } from "react-router-dom";

const Home = () => {
  const params = useParams();
  return (
    <div>
      <h1>Home 페이지</h1>
      <p>여기는 시작하기 버튼을 눌러 이동한 Home 페이지입니다.</p>
    </div>
  );
};

export default Home;
