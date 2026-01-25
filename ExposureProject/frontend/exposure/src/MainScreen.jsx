import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL;

export default function MainScreen() {
  const navigate = useNavigate();
  const [bots, setBots] = useState([]);
  const [selectedBotIds, setSelectedBotIds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [buttonText, setButtonText] = useState("Играть");
  const [isActive, setIsActive] = useState(false);
  const [isGameLoading, setIsGameLoading] = useState(false);

  const userToken = localStorage.getItem('token');

  useEffect(() => {
    const initializePage = async () => {
      try {
        
        await axios.get(API_URL + '/api/main', {
          headers: { Authorization: userToken }
        });

        const response = await axios.get(API_URL + '/api/main/bots', {
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
        prev = prev.filter(botId => botId !== id)
      } else if (prev.length < 2) {
        prev = [...prev, id];
      }

      if (prev.length == 2) {
        setIsActive(true);
      } else {
        setIsActive(false);
      }

      return prev;
    });
  };

  const startGame = async () => {
    if (selectedBotIds.length != 2) {
      return;
    }

    if (!isActive) {
      return;
    }

    try {
      setButtonText("Загрузка...");
      setIsActive(false);
      setIsGameLoading(true);

      const response = await axios.post(API_URL + '/api/game/start', {
        userId: userToken,
        selectedBotId: selectedBotIds // Отправляем массив ID
      });
      
      navigate('/game', { state: { session: response.data } });
    } catch (error) {
      console.error("Ошибка старта игры", error);
      alert("Не удалось начать игру");
      setButtonText("Играть");
      setIsActive(true);
      setIsGameLoading(false);
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
                className={isGameLoading? '.disabled-div' : ''}
                style={{
                  border: isSelected ? '3px solid #28a745' : '1px solid gray',
                  padding: '35px',
                  marginTop: "20px",
                  borderRadius: '12px',
                  backgroundColor: isSelected ? '#e6ffed' : 'white',
                  transition: '0.2s',
                  transform: isSelected ? 'scale(1.05)' : 'scale(1)',
                  pointerEvents: isGameLoading? 'none' : '',
                  opacity: isGameLoading? '0.5' : '1',
                  cursor: isGameLoading? 'not-allowed' : 'pointer'
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
        className="btn btn-primary btn-lg" type="button" disabled={!isActive}>
        <span className={isGameLoading? 'spinner-border spinner-border-sm' : 'spinner-border spinner-border-sm d-none'} role="status" aria-hidden="true"></span>
        {isGameLoading? '   ': ""}{buttonText}
      </button>
    </div>
  );
}
