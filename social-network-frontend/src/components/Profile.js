import axios from 'axios';
import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import './Profile.css';

function Profile() {
    const { userId } = useParams();
    const [user, setUser] = useState(null);
    const [followers, setFollowers] = useState([]);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    const fetchUser = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/users/${userId}`, {
                withCredentials: true
            });
            setUser(response.data);
        } catch (error) {
            setError('Failed to load user');
        }
    };

    const fetchFollowers = async () => {
        try {
            const response = await axios.get(`http://localhost:8080/api/followers/user/${userId}`, {
                withCredentials: true
            });
            setFollowers(response.data);
        } catch (error) {
            setError('Failed to load followers');
        }
    };

    const handleFollow = async () => {
        try {
            const response = await axios.post(`http://localhost:8080/api/followers/user/${userId}`,
                {},
                { withCredentials: true }
            );
            setSuccess(response.data);
            fetchFollowers();
        } catch (error) {
            setError(error.response?.data || 'Failed to follow user');
        }
    };

    const handleUnfollow = async () => {
        try {
            const response = await axios.delete(`http://localhost:8080/api/followers/user/${userId}`,
                { withCredentials: true }
            );
            setSuccess(response.data);
            fetchFollowers();
        } catch (error) {
            setError(error.response?.data || 'Failed to unfollow user');
        }
    };

    useEffect(() => {
        fetchUser();
        fetchFollowers();
    }, [userId]);

    if (!user) {
        return <div>Loading...</div>;
    }

    return (
        <div className="profile-container">
            <h2>{user.username}'s Profile</h2>
            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}
            <p>Email: {user.email}</p>
            <p>Bio: {user.bio || 'No bio'}</p>
            <p>Joined: {new Date(user.createdAt).toLocaleString()}</p>
            <div className="followers-section">
                <p>Followers: {followers.length}</p>
                <button onClick={handleFollow}>Follow</button>
                <button onClick={handleUnfollow}>Unfollow</button>
            </div>
            <div className="followers-list">
                <h4>Followers</h4>
                {followers.map(follower => (
                    <p key={follower.followerId}>{follower.follower.username}</p>
                ))}
            </div>
        </div>
    );
}

export default Profile;