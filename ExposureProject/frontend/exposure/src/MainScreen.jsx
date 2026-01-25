import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function MainScreen() {
  const navigate = useNavigate();
  const [bots, setBots] = useState([]);
  const [selectedBotIds, setSelectedBotIds] = useState([]);
  const [loading, setLoading] = useState(true);

  const userToken = localStorage.getItem('token');

  useEffect(() => {
    const initializePage = async () => {
      try {
        await axios.get('http://localhost:8080/api/main', {
          headers: { Authorization: userToken }
        });

        const response = await axios.get('http://localhost:8080/api/main/bots', {
          headers: { Authorization: userToken }
        });
        
        setBots(response.data);
        setLoading(false);
      } catch (error) {
        console.error("Ошибка при инициализации страницы или загрузке ботов", error);
        setLoading(false);
      }
    };

    if (userToken) {
      initializePage();
    }
  }, [userToken]);

  const toggleBotSelection = (id) => {
    setSelectedBotIds((prev) => {
      if (prev.includes(id)) {
        return prev.filter(botId => botId !== id);
      } else if (prev.length < 2) {
        return [...prev, id];
      } else {
        return prev;
      }
    });
  };

  const startGame = async () => {
    if (selectedBotIds.length !== 2) {
      alert("Нужно выбрать ровно двух ботов!");
      return;
    }

    try {
      const response = await axios.post('http://localhost:8080/api/game/start', {
        userId: userToken,
        selectedBotId: selectedBotIds // Отправляем массив ID
      });
      
      navigate('/game', { state: { session: response.data } });
    } catch (error) {
      console.error("Ошибка старта игры", error);
      alert("Не удалось начать игру");
    }
  };

  if (loading) return <div>Загрузка ботов...</div>;


  // TODO: Вынести это в CSS файл после.
  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h1>Кто лжет?</h1>
      
      <div style={{ margin: '20px' }}>
        <h3>Выберите двух противников ({selectedBotIds.length}/2):</h3>
        <div style={{ display: 'flex', justifyContent: 'center', gap: '15px', flexWrap: 'wrap' }}>
          {bots.map((bot) => {
            const isSelected = selectedBotIds.includes(bot.id);
            return (
              <div 
                key={bot.id}
                onClick={() => toggleBotSelection(bot.id)}
                style={{
                  border: isSelected ? '3px solid #28a745' : '1px solid gray',
                  padding: '15px',
                  cursor: 'pointer',
                  borderRadius: '12px',
                  backgroundColor: isSelected ? '#e6ffed' : 'white',
                  transition: '0.2s',
                  transform: isSelected ? 'scale(1.05)' : 'scale(1)'
                }}
              >
                <div style={{ fontSize: '30px' }}>🤖</div>
                {bot.name}
              </div>
            );
          })}
        </div>
      </div>

      <button 
        onClick={startGame} 
        disabled={selectedBotIds.length !== 2}
        style={{ 
          padding: '10px 30px', 
          fontSize: '20px', 
          marginTop: '20px',
          backgroundColor: selectedBotIds.length === 2 ? '#007bff' : '#ccc',
          color: 'white',
          border: 'none',
          borderRadius: '5px',
          cursor: selectedBotIds.length === 2 ? 'pointer' : 'not-allowed'
        }}
      >
        Играть втроем
      </button>
    </div>
  );
}
