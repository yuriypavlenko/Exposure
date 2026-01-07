// GameScreen.jsx
import { useLocation, useNavigate } from 'react-router-dom';
import { useState } from 'react';
import axios from 'axios';


/*

Итак, проблема фронта сейчас что вопрос один для каждого бота. Нужно разделить - один вопрос на одно окно на одного бота в одной сессии.
Еще одна проблема - количество вопросов. С бэка должно приходить количество вопросов на сессию (см. Mainscreen) а здесь должно
проверяться на фронте и бэк тоже должен перепроверить и если отправляет определенный ответ, допустим 403 - это означает что игра закончена
что вопросов не осталось и значит игрок должен выбрать кому доверяет уже не в состоянии спрашивать вопросы.
После удачного запроса на сервер с вопросом сервер отправляет ему ответ и количество вопросов которое осталось. Это важно.

Если говорить про выбор, то кажется на фронте вообще нет такой опции пока что. Нужно добавить. Смысл в том что мы
добавляем просто кнопку "доверяю" к кажому боту и при нажатии отправляется наш выбор на адрес


При вопросе должно отправляться айди игрока (токен), айди бота кому отправлен вопрос, айди сессии и сам вопрос.

Добавить вывод количества вопросов что осталось.

*/


export default function GameScreen() {
  const { state } = useLocation();
  const navigate = useNavigate();
  const { sessionId, bots } = state.session;

  const [questions, setQuestions] = useState('');
  const [chats, setChats] = useState({}); // Храним историю: { botId: [{q, a}] }

  const handleExit = async () => {
    await axios.post('http://localhost:8080/api/game/endsession', { sessionId });
    navigate('/');
  };

  const askQuestion = async (botId) => {
    if (!questions) return;
    
    try {
      const res = await axios.post('http://localhost:8080/api/game/question', {
        botId,
        sessionId,
        question: questions
      });

      setChats(prev => ({
        ...prev,
        [botId]: [...(prev[botId] || []), { q: questions, a: res.data.answer }]
      }));
      setQuestions('');
    } catch (e) {
      console.error("Ошибка при вопросе", e);
    }
  };

  return (
    <div style={{ padding: '20px' }}>
      <button onClick={handleExit}>Выйти из игры</button>
      <h2>ID Сессии: {sessionId}</h2>

      <div style={{ display: 'flex', gap: '20px', marginTop: '20px' }}>
        {bots.map(bot => (
          <div key={bot.id} style={{ border: '1px solid black', padding: '10px', flex: 1 }}>
            <h3>{bot.name} (ID: {bot.id})</h3>
            <div style={{ height: '200px', overflowY: 'auto', background: '#f0f0f0', marginBottom: '10px' }}>
              {chats[bot.id]?.map((msg, i) => (
                <p key={i}><b>Вы:</b> {msg.q}<br/><b>Бот:</b> {msg.a}</p>
              ))}
            </div>
            <input 
              value={questions} 
              onChange={(e) => setQuestions(e.target.value)} 
              placeholder="Введите вопрос..." 
            />
            <button onClick={() => askQuestion(bot.id)}>Спросить</button>
          </div>
        ))}
      </div>
    </div>
  );
}
