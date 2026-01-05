// MainScreen.jsx
import { useNavigate } from 'react-router-dom';
import axios from 'axios';


/*

Проблема этого скрипта в том, что мы нажимаем кнопку, но по факту ничего не отсылаем фронту.
Нам в первую очередь нужно отсылать фронту выбор, как сделать выбор в ресторане когда официант пришел.
Когда страница открыта нам нужно послать запрос на список ботов, которые мы выбирем.

1) Сделать запрос когда мы открываем страницу
2) Разложить на фронте
3) Пользователь делает выбор - показать
4) Когда подтверждаем выбор кидаем его на игру отправляя запрос.

*/


export default function MainScreen() {
  const navigate = useNavigate();

  const startGame = async () => {
    try {
      // Бекенд должен вернуть: { sessionId: 1, bots: [{id: 1, name: 'Бот А'}, {id: 2, name: 'Бот Б'}] }
      const response = await axios.post('http://localhost:8080/api/game/start');
      navigate('/game', { state: { session: response.data } });
    } catch (error) {
      console.error("Ошибка старта игры", error);
      alert("Не удалось связаться с сервером");
    }
  };

  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h1>Кто лжет?</h1>
      <button onClick={startGame} style={{ padding: '10px 20px', fontSize: '20px' }}>
        Играть
      </button>
    </div>
  );
}
