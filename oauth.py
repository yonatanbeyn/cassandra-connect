✅ Implementation
python
Copy
Edit
import time
import requests
from functools import wraps

# Global cache (you can also use something more advanced like functools.lru_cache or a cache lib)
_token_cache = {}

def oauth_token_decorator(token_url, client_id, client_secret, username, password, scope):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            cache_key = f"{client_id}:{username}:{scope}"
            token_data = _token_cache.get(cache_key)

            if not token_data or token_data["expires_at"] < time.time():
                # Request a new token
                payload = {
                    "grant_type": "password",
                    "client_id": client_id,
                    "client_secret": client_secret,
                    "username": username,
                    "password": password,
                    "scope": scope
                }
                headers = {
                    "Content-Type": "application/x-www-form-urlencoded"
                }

                response = requests.post(token_url, data=payload, headers=headers)
                response.raise_for_status()

                token_response = response.json()
                access_token = token_response["access_token"]
                expires_in = token_response.get("expires_in", 3600)

                token_data = {
                    "access_token": access_token,
                    "expires_at": time.time() + expires_in * 0.9  # refresh slightly before expiry
                }
                _token_cache[cache_key] = token_data

            # Inject token into headers
            headers = kwargs.pop("headers", {})
            headers["Authorization"] = f"Bearer {token_data['access_token']}"
            kwargs["headers"] = headers

            return func(*args, **kwargs)
        return wrapper
    return decorator
✅ Example Usage
python
Copy
Edit
@oauth_token_decorator(
    token_url="https://auth.example.com/oauth/token",
    client_id="your-client-id",
    client_secret="your-client-secret",
    username="user@example.com",
    password="your-password",
    scope="read write"
)
def call_protected_api(url, **kwargs):
    response = requests.get(url, **kwargs)
    return response.json()

# Example call
data = call_protected_api("https://api.example.com/protected-resource")
print(data)
