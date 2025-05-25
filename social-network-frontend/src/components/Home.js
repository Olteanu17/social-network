import axios from 'axios';
import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './Home.css';

function Home() {
    const [currentUser, setCurrentUser] = useState(null);
    const [recentPosts, setRecentPosts] = useState([]);
    const [recentMessages, setRecentMessages] = useState([]);
    const [error, setError] = useState('');

    // Fetch current user
    const fetchCurrentUser = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/users/me', { withCredentials: true });
            setCurrentUser(response.data);
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : 'Failed to load user data');
        }
    };

    // Fetch recent posts
    const fetchRecentPosts = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/posts', { withCredentials: true });
            const sortedPosts = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 3);
            setRecentPosts(sortedPosts);
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : 'Failed to load recent posts');
        }
    };

    // Fetch recent messages received by current user
    const fetchRecentMessages = async () => {
        try {
            const usersResponse = await axios.get('http://localhost:8080/api/messages/users/names', { withCredentials: true });
            const users = usersResponse.data;
            let allMessages = [];
            for (const user of users) {
                const messagesResponse = await axios.get(`http://localhost:8080/api/messages/conversation/${user.id}`, { withCredentials: true });
                allMessages = allMessages.concat(messagesResponse.data.filter(msg => msg.receiver.id === currentUser?.id));
            }
            const sortedMessages = allMessages.sort((a, b) => new Date(b.sentAt) - new Date(a.sentAt)).slice(0, 3);
            setRecentMessages(sortedMessages);
        } catch (error) {
            setError(typeof error.response?.data === 'string' ? error.response.data : 'Failed to load recent messages');
        }
    };

    const handleLogout = async () => {
        try {
            await axios.post('http://localhost:8080/api/auth/logout', {}, { withCredentials: true });
            window.location.href = '/login';
        } catch (error) {
            alert('Logout failed');
        }
    };

    useEffect(() => {
        fetchCurrentUser();
    }, []);

    useEffect(() => {
        if (currentUser) {
            fetchRecentPosts();
            fetchRecentMessages();
        }
    }, [currentUser]);

    return (
        <div className="home-container">
            <h2>{currentUser ? `Welcome, ${currentUser.username}!` : 'Welcome to Home!'}</h2>
            {error && <p className="error">{error}</p>}

            {/* Quick Navigation */}
            <div className="quick-nav">
                <Link to="/posts" className="nav-button">View Posts</Link>
                <Link to="/messages" className="nav-button">Messages</Link>
                <Link to="/profile" className="nav-button">Profile</Link>
            </div>

            {/* Recent Notifications */}
            <div className="recent-notifications">
                <h3>Recent Notifications</h3>
                <div className="notifications-list">
                    {/* Recent Posts */}
                    <div className="notification-section">
                        <h4>Latest Posts</h4>
                        {recentPosts.length > 0 ? (
                            recentPosts.map(post => (
                                <div key={post.id} className="notification-item">
                                    <p><strong>{post.user.username}</strong>: {post.content}</p>
                                    {post.imageUrl && (
                                        <div className="post-image-container">
                                            <img
                                                src={post.imageUrl}
                                                alt={`${post.user.username}'s post`}
                                                className="post-image"
                                                onError={(e) => console.error('Image failed to load:', post.imageUrl)}
                                            />
                                        </div>
                                    )}
                                    <p className="notification-meta">{new Date(post.createdAt).toLocaleString()}</p>
                                </div>
                            ))
                        ) : (
                            <p>No recent posts available.</p>
                        )}
                    </div>

                    {/* Recent Messages */}
                    <div className="notification-section">
                        <h4>Latest Messages</h4>
                        {recentMessages.length > 0 ? (
                            recentMessages.map(message => (
                                <div key={message.id} className="notification-item">
                                    <p><strong>{message.sender.username}</strong>: {message.content}</p>
                                    <p className="notification-meta">{new Date(message.sentAt).toLocaleString()}</p>
                                </div>
                            ))
                        ) : (
                            <p>No recent messages available.</p>
                        )}
                    </div>
                </div>
            </div>

            {/* Logout Button */}
            <button onClick={handleLogout} className="logout-button">Logout</button>
        </div>
    );
}

export default Home;