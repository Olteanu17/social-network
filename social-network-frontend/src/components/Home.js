import axios from 'axios';

function Home() {
    const handleLogout = async () => {
        try {
            await axios.post('http://localhost:8080/api/auth/logout', {}, {
                withCredentials: true
            });
            window.location.href = '/login';
        } catch (error) {
            alert('Logout failed');
        }
    };

    return (
        <div>
            <h2>Welcome to Home!</h2>
            <button onClick={handleLogout}>Logout</button>
        </div>
    );
}

export default Home;