import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

const ResultScreen = () => {
    const location = useLocation();
    const navigate = useNavigate();
    
    const { isLiar } = location.state || {};

    const handleGoHome = () => {
        navigate('/');
    };

    return (
        <div style={styles.container}>
            <h1 style={styles.title}>Результаты игры</h1>
            
            <div style={styles.card}>
                {isLiar ? (
                    <div style={styles.success}>
                        <h2>🎉 Вы победили!</h2>
                        <p>Вы успешно разоблачили лжеца. Ваша интуиция вас не подвела!</p>
                    </div>
                ) : (
                    <div style={styles.error}>
                        <h2>💀 Ошибка!</h2>
                        <p>Этот бот говорил правду. Настоящий лжец остался в тени...</p>
                    </div>
                )}
            </div>

            <button onClick={handleGoHome} style={styles.button}>
                Вернуться на главную
            </button>
        </div>
    );
};

const styles = {
    container: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100vh',
        fontFamily: 'Arial, sans-serif',
        backgroundColor: '#f0f2f5'
    },
    card: {
        background: 'white',
        padding: '30px',
        borderRadius: '12px',
        boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
        textAlign: 'center',
        marginBottom: '20px',
        maxWidth: '400px'
    },
    success: {
        color: '#2e7d32'
    },
    error: {
        color: '#d32f2f'
    },
    button: {
        padding: '10px 20px',
        fontSize: '16px',
        cursor: 'pointer',
        borderRadius: '8px',
        border: 'none',
        backgroundColor: '#007bff',
        color: 'white'
    }
};

export default ResultScreen;
