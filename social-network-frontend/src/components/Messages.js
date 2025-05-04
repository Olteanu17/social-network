import axios from 'axios';
import { useState, useEffect } from 'react';
import './Messages.css';

function Messages() {
    const [receiverId, setReceiverId] = useState('');
    const [content, setContent] = useState('');
    const [messages, setMessages] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const fetchMessages = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/messages/conversation/${receiverId}`, {
                withCredentials: true
            });
            setMessages(response.data);
        } catch (error) {
            setError('Failed to load messages');
        }
    };

    const handleSendMessage = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const response = await axios.post('http://localhost:8080/api/messages',
                { receiverId, content },
                { withCredentials: true }
            );
            setSuccess(response.data);
            setContent('');
            fetchMessages();
        } catch (error) {
            setError(error.response?.data || 'Failed to send message');
        }
    };

    useEffect(() => {
        if (receiverId) {
            fetchMessages();
        }
    }, [receiverId]);

    return (
        <div className="messages-container">
            <h2>Messages</h2>
            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}
            <div>
                <label>Receiver ID:</label>
                <input
                    type="number"
                    value={receiverId}
                    onChange={(e) => setReceiverId(e.target.value)}
                    placeholder="Enter receiver ID"
                />
            </div>
            <form onSubmit={handleSendMessage}>
                <div>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        placeholder="Type your message..."
                        required
                    />
                </div>
                <button type="submit">Send</button>
            </form>
            <div className="messages-list">
                {messages.map(message => (
                    <div key={message.id} className="message">
                        <p><strong>{message.sender.username}</strong> to <strong>{message.receiver.username}</strong>: {message.content}</p>
                        <p>{new Date(message.sentAt).toLocaleString()} {message.isRead ? '(Read)' : '(Unread)'}</p>
                    </div>
                ))}
            </div>
        </div>
    );
}

export default Messages;