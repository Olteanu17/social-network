import axios from 'axios';
import { useState, useEffect } from 'react';
import './Profile.css';

function Profile() {
    const [currentUser, setCurrentUser] = useState(null);
    const [viewedUser, setViewedUser] = useState(null);
    const [followers, setFollowers] = useState([]);
    const [users, setUsers] = useState([]);
    const [selectedUserId, setSelectedUserId] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [showBioForm, setShowBioForm] = useState(false);
    const [newBio, setNewBio] = useState('');

    const fetchCurrentUser = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/users/me', {
                withCredentials: true
            });
            setCurrentUser(response.data);
            setViewedUser(response.data);
            setNewBio(response.data.bio || '');
            fetchFollowers(response.data.id);
        } catch (error) {
            setError('Failed to load current user');
        }
    };

    const fetchViewedUser = async (userId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/users/${userId}`, {
                withCredentials: true
            });
            setViewedUser(response.data);
            fetchFollowers(response.data.id);
        } catch (error) {
            setError('Failed to load user profile');
        }
    };

    const fetchFollowers = async (userId) => {
        try {
            const response = await axios.get(`http://localhost:8080/api/followers/user/${userId}`, {
                withCredentials: true
            });
            setFollowers(response.data);
        } catch (error) {
            setError('Failed to load followers');
        }
    };

    const fetchUsers = async () => {
        try {
            const response = await axios.get('http://localhost:8080/api/users/names', {
                withCredentials: true
            });
            setUsers(response.data);
        } catch (error) {
            setError('Failed to load users');
        }
    };

    const handleUpdateBio = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');

        try {
            const formData = new FormData();
            formData.append('bio', newBio);

            const response = await axios.put('http://localhost:8080/api/users/me',
                formData,
                { withCredentials: true }
            );
            setSuccess(response.data);
            setShowBioForm(false);
            fetchCurrentUser();
        } catch (error) {
            setError(error.response?.data || 'Failed to update bio');
        }
    };

    const handleMakeAdmin = async () => {
        try {
            const response = await axios.put(`http://localhost:8080/api/users/${viewedUser.id}/make-admin`, {}, {
                withCredentials: true
            });
            setSuccess(response.data);
            fetchViewedUser(viewedUser.id);
        } catch (error) {
            setError(error.response?.data || 'Failed to make user admin');
        }
    };

    const handleDeleteUser = async () => {
        try {
            const response = await axios.delete(`http://localhost:8080/api/users/${viewedUser.id}`, {
                withCredentials: true
            });
            setSuccess(response.data);
            setViewedUser(null);
            fetchCurrentUser();
        } catch (error) {
            setError(error.response?.data || 'Failed to delete user');
        }
    };

    const handleFollow = async () => {
        try {
            const response = await axios.post(`http://localhost:8080/api/followers/user/${viewedUser.id}`,
                {},
                { withCredentials: true }
            );
            setSuccess(response.data);
            fetchFollowers(viewedUser.id);
        } catch (error) {
            setError(error.response?.data || 'Failed to follow user');
        }
    };

    const handleUnfollow = async () => {
        try {
            const response = await axios.delete(`http://localhost:8080/api/followers/user/${viewedUser.id}`,
                { withCredentials: true }
            );
            setSuccess(response.data);
            fetchFollowers(viewedUser.id);
        } catch (error) {
            setError(error.response?.data || 'Failed to unfollow user');
        }
    };

    const handleUserSelect = () => {
        if (selectedUserId) {
            fetchViewedUser(selectedUserId);
        } else {
            fetchCurrentUser();
        }
    };

    useEffect(() => {
        fetchCurrentUser();
        fetchUsers();
    }, []);

    if (!viewedUser) {
        return <div>Loading...</div>;
    }

    const isCurrentUserProfile = currentUser && viewedUser && currentUser.id === viewedUser.id;
    const isAdmin = currentUser?.admin || false;
    const isNotSelfProfile = !isCurrentUserProfile;

    return (
        <div className="profile-container">
            <h2>{viewedUser.username}'s Profile</h2>
            {error && <p className="error">{error}</p>}
            {success && <p className="success">{success}</p>}
            <div className="user-selector">
                <h4>Select a User</h4>
                <select
                    value={selectedUserId}
                    onChange={(e) => setSelectedUserId(e.target.value)}
                >
                    <option value="">My Profile</option>
                    {users.map(user => (
                        <option key={user.id} value={user.id}>{user.username} {user.isAdmin ? '(Admin)' : ''}</option>
                    ))}
                </select>
                <button onClick={handleUserSelect}>View Profile</button>
            </div>
            <div className="profile-content">
                <div className="profile-details">
                    <p><strong>Email:</strong> {viewedUser.email}</p>
                    <p><strong>Bio:</strong> {viewedUser.bio || 'No bio'}</p>
                    <p><strong>Joined:</strong> {new Date(viewedUser.createdAt).toLocaleString()}</p>
                    <p><strong>Followers:</strong> {followers.length}</p>
                    <p><strong>Admin:</strong> {viewedUser.admin ? 'Yes' : 'No'}</p>
                </div>
            </div>
            {isCurrentUserProfile ? (
                <div className="edit-profile-buttons">
                    <button onClick={() => setShowBioForm(!showBioForm)}>
                        {showBioForm ? 'Cancel' : 'Change Bio'}
                    </button>
                </div>
            ) : (
                <div className="follow-buttons">
                    <button onClick={handleFollow}>Follow</button>
                    <button onClick={handleUnfollow}>Unfollow</button>
                    {isAdmin && isNotSelfProfile && (
                        <>
                            <button onClick={handleMakeAdmin}>Make Administrator</button>
                            <button onClick={handleDeleteUser}>Delete</button>
                        </>
                    )}
                </div>
            )}
            {showBioForm && isCurrentUserProfile && (
                <form onSubmit={handleUpdateBio} className="edit-bio-form">
                    <div>
                        <label>Bio:</label>
                        <textarea
                            value={newBio}
                            onChange={(e) => setNewBio(e.target.value)}
                            placeholder="Update your bio"
                        />
                    </div>
                    <button type="submit">Save Bio</button>
                </form>
            )}
        </div>
    );
}

export default Profile;