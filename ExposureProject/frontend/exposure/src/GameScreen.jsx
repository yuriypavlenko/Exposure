import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL;

export default function GameScreen() {
  const { state } = useLocation();
  const navigate = useNavigate();
  
  const sessionData = state?.session || {};
  const { 
    sessionId: sessionId,
    userId: userId,
    bots: bots = [], 
    questionsLeft: initialQuestions 
  } = sessionData;

  const [remainingQuestions, setRemainingQuestions] = useState(initialQuestions ?? 0);
  const [inputs, setInputs] = useState({});
  const [chats, setChats] = useState({});
  const [isGameOver, setIsGameOver] = useState(false);

  // Следим за остатком вопросов для вывода уведомления
  useEffect(() => {
    if (remainingQuestions === 0 && !isGameOver) {
      alert("Внимание: вопросы закончились! Теперь вы должны выбрать, кому доверяете.");
      setIsGameOver(true);
    }
  }, [remainingQuestions, isGameOver]);

  const handleInputChange = (botId, value) => {
    setInputs(prev => ({ ...prev, [botId]: value }));
  };

  const askQuestion = async (botId) => {
    if (!sessionId || !userId) {
        alert("Ошибка: сессия не инициализирована");
        return;
    }

    const questionText = inputs[botId];
    if (!questionText || remainingQuestions <= 0) return;
    
    try {
      const res = await axios.post(API_URL + '/api/game/question', {
        userId,
        botId,
        sessionId,
        question: questionText
      });

      // В ответе бэка теперь используем res.data.questionsLeft (проверьте соответствие с DTO)
      const newCount = res.data.questionsLeft;

      setChats(prev => ({
        ...prev,
        [botId]: [...(prev[botId] || []), { q: questionText, a: res.data.answer }]
      }));
      
      setRemainingQuestions(newCount);
      setInputs(prev => ({ ...prev, [botId]: '' }));
      
    } catch (e) {
      handleError(e);
    }
  };

  const handleChoice = async (botId) => {
    try {
        const response = await axios.post(API_URL + '/api/game/choice', { userId, botId, sessionId });
        
        // Передаем данные о том, был ли бот лжецом, через state
        navigate('/results', { 
            state: { 
                isLiar: response.data.isLiar, // Проверь имя поля в твоем ChoiceResponse
                botId: botId 
            } 
        });
    } catch (e) {
        handleError(e);
    }

     // Централизованная обработка ошибок бэка
  const handleError = (e) => {
    const status = e.response?.status;
    if (status === 403) {
      setRemainingQuestions(0);
      alert("Доступ запрещен или вопросы исчерпаны. Пожалуйста, сделайте выбор.");
    } else if (status === 400) {
      alert("Ошибка запроса: проверьте корректность данных.");
    } else {
      alert("Произошла системная ошибка. Попробуйте позже.");
    }
    console.error("Game Error:", e);
  };
};


  const handleExit = () => {
    if (window.confirm("Вы уверены, что хотите выйти? Прогресс будет потерян.")) {
      navigate('/');
    }
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <button onClick={handleExit} style={{ padding: '10px' }}>Покинуть игру</button>
        
        <div style={{ textAlign: 'right' }}>
          <h2 style={{ margin: 0, color: remainingQuestions <= 0 ? '#ff4d4f' : '#2f54eb' }}>
            Осталось вопросов: {remainingQuestions}
          </h2>
          {remainingQuestions <= 0 && <small style={{ color: 'red' }}>Время делать выбор!</small>}
        </div>
      </div>

      <div style={{ display: 'flex', gap: '20px' }}>
        {bots.map(bot => (
          <div key={bot.id} style={{ 
            border: '2px solid #f0f0f0', 
            padding: '20px', 
            flex: 1, 
            borderRadius: '12px',
            backgroundColor: 'white',
            boxShadow: '0 4px 6px rgba(0,0,0,0.05)'
          }}>
            <h3 style={{ borderBottom: '1px solid #eee', pb: '10px' }}>{bot.name}</h3>
            
            <div style={{ 
              height: '300px', 
              overflowY: 'auto', 
              background: '#fafafa', 
              padding: '10px', 
              marginBottom: '15px',
              borderRadius: '8px',
              border: '1px solid #eee'
            }}>
              {chats[bot.id]?.length > 0 ? (
                chats[bot.id].map((msg, i) => (
                  <div key={i} style={{ marginBottom: '15px' }}>
                    <div style={{ fontSize: '0.85em', color: '#888' }}>Вы:</div>
                    <div style={{ background: '#e6f7ff', padding: '8px', borderRadius: '8px', marginBottom: '5px' }}>{msg.q}</div>
                    <div style={{ fontSize: '0.85em', color: '#888' }}>{bot.name}:</div>
                    <div style={{ background: '#f6ffed', padding: '8px', borderRadius: '8px' }}>{msg.a}</div>
                  </div>
                ))
              ) : (
                <div style={{ color: '#ccc', textAlign: 'center', marginTop: '100px' }}>Нет сообщений</div>
              )}
            </div>

            <div style={{ display: 'flex', gap: '5px', marginBottom: '20px' }}>
              <input 
                value={inputs[bot.id] || ''} 
                onChange={(e) => handleInputChange(bot.id, e.target.value)} 
                placeholder={remainingQuestions > 0 ? "Спросить бота..." : "Вопросы закончились"}
                disabled={remainingQuestions <= 0}
                style={{ flex: 1, padding: '10px', borderRadius: '4px', border: '1px solid #d9d9d9' }}
                onKeyPress={(e) => e.key === 'Enter' && askQuestion(bot.id)}
              />
              <button 
                onClick={() => askQuestion(bot.id)} 
                disabled={remainingQuestions <= 0 || !inputs[bot.id]}
                style={{ padding: '0 15px', cursor: remainingQuestions <= 0 ? 'not-allowed' : 'pointer' }}
              >
                Отправить
              </button>
            </div>

            <button 
              onClick={() => handleChoice(bot.id)}
              style={{ 
                width: '100%', 
                background: '#52c41a', 
                color: 'white', 
                border: 'none', 
                padding: '12px', 
                borderRadius: '6px',
                fontWeight: 'bold',
                cursor: 'pointer',
                transition: '0.3s'
              }}
              onMouseOver={(e) => e.target.style.background = '#73d13d'}
              onMouseOut={(e) => e.target.style.background = '#52c41a'}
            >
              ОБВИНИТЬ {bot.name.toUpperCase()}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
