import axios from 'axios';
import { useState, useEffect } from 'react';
import './Messages.css';

function Messages() {
    const [receiverId, setReceiverId] = useState('');
    const [content, setContent] = useState('');
    const [messages, setMessages] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [users, setUsers] = useState([]);
    const [currentUserId, setCurrentUserId] = useState(null);

    const fetchMessages = async () => {
        if (!receiverId) return;
        try {
            const response = await axios.get(`http://localhost:8080/api/messages/conversation/${receiverId}`, {
                withCredentials: true
            });
            setMessages(response.data);
        } catch (error) {
            setError('Failed to load messages');
        }
    };

    const fetchUsers = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/messages/users/names', {
                withCredentials: true
            });
            setUsers(response.data);
        } catch (error) {
            setError('Failed to load users');
        }
    };

    const fetchCurrentUserId = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/messages/current-id', {
                withCredentials: true
            });
            setCurrentUserId(response.data);
        } catch (error) {
            setError('Failed to load current user ID');
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

    const handleUserSelect = (userId) => {
        setReceiverId(userId);
    };

    useEffect(() => {
        fetchUsers();
        fetchCurrentUserId();
        if (receiverId) {
            fetchMessages();
        }
    }, [receiverId]);

    return (
        <div className="messages-container">
            <h2>Messages</h2>
            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}
            <div className="messages-layout">
                <div className="users-list">
                    {users.map(user => (
                        <div
                            key={user.id}
                            className={`user-item ${receiverId === user.id.toString() ? 'selected' : ''}`}
                            onClick={() => handleUserSelect(user.id.toString())}
                        >
                            <p>{user.username}</p>
                        </div>
                    ))}
                </div>
                {receiverId && (
                    <div className="conversation-container">
                        <div className="messages-list">
                            {messages.map(message => {
                                const isSentByCurrentUser = message.sender.id.toString() === currentUserId.toString();
                                const senderName = isSentByCurrentUser ? 'You' : message.sender.username;
                                return (
                                    <div key={message.id} className={`message ${isSentByCurrentUser ? 'sent' : 'received'}`}>
                                        <p><strong>{senderName}:</strong> {message.content}</p>
                                        <p>{new Date(message.sentAt).toLocaleString()}</p>
                                    </div>
                                );
                            })}
                        </div>
                        <form onSubmit={handleSendMessage}>
                            <div className="message-input">
                                <textarea
                                    value={content}
                                    onChange={(e) => setContent(e.target.value)}
                                    placeholder="Type your message..."
                                    required
                                />
                                <button type="submit">Send</button>
                            </div>
                        </form>
                    </div>
                )}
            </div>
        </div>
    );
}

export default Messages;