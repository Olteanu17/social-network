import React, { useEffect, useState } from 'react';
import './App.css';

function App() {
  const [message, setMessage] = useState('');

  useEffect(() => {
    fetch('http://localhost:8080/hello')
        .then(response => response.text())
        .then(data => setMessage(data))
        .catch(error => console.error('Error:', error));
  }, []);

  return (
      <div className="App">
        <header className="App-header">
          <h1>Social Network</h1>
          <p>{message || 'Loading...'}</p>
        </header>
      </div>
  );
}

export default App;