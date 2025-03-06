# Testing Jellyfin Integration

## Prerequisites
1. Set up a Jellyfin server (v10.8.0 or later)
2. Add test media content to the server
3. Create a test user account
4. Enable remote access (if testing outside local network)

## Server Setup
1. Install Jellyfin:
```bash
# Docker
docker run -d \
 --name jellyfin \
 -p 8096:8096 \
 -v /path/to/config:/config \
 -v /path/to/media:/media \
 jellyfin/jellyfin:latest
```

2. Access server at `http://localhost:8096`
3. Create test libraries:
   - Movies (add some sample movies)
   - TV Shows (add some sample episodes)
   - Music (optional)

## Test Account Setup
1. Create test user:
   - Username: `syncplaytest`
   - Password: `testpass123`
   - Permissions: 
     - Allow media playback
     - Allow remote connections
     - Allow media downloads

## Test Scenarios

### 1. Basic Authentication
```
Server URL: http://<server-ip>:8096
Username: syncplaytest
Password: testpass123
```
Expected: Successful login and transition to media browser

### 2. Media Library Access
1. Verify all libraries are visible
2. Check thumbnail loading
3. Verify library filtering works

### 3. Media Playback
1. Select a movie/episode
2. Verify stream URL generation
3. Check playback starts in Syncplay room
4. Verify seek/pause controls work

### 4. Error Cases
Test the following scenarios:
- Wrong server URL
- Invalid credentials
- Network disconnection
- Server unavailable
- Insufficient permissions

### 5. Performance Testing
1. Large libraries (100+ items)
2. Multiple concurrent streams
3. Different media formats
4. Network conditions:
   - High latency
   - Low bandwidth
   - Intermittent connectivity

## Integration Test Steps

1. Server Connection:
```kotlin
val repository = JellyfinRepositoryImpl()
val result = repository.authenticate(
    serverUrl = "http://<server-ip>:8096",
    username = "syncplaytest",
    password = "testpass123"
)
assert(result.isSuccess)
```

2. Library Access:
```kotlin
val libraries = repository.getLibraries().getOrNull()
assert(libraries?.isNotEmpty() == true)
assert(libraries?.any { it.name == "Movies" } == true)
```

3. Media Streaming:
```kotlin
// Get first movie
val movies = repository.getMediaItems(libraries[0].id).getOrNull()
val streamUrl = repository.getStreamUrl(movies[0].id).getOrNull()
assert(streamUrl?.isNotEmpty() == true)
```

## Troubleshooting

### Common Issues
1. HTTPS Certificate Errors
   - Add server certificate to trusted store
   - Use HTTP for local testing

2. Authentication Failures
   - Verify API key permissions
   - Check network access to server
   - Confirm user account status

3. Streaming Issues
   - Check media format compatibility
   - Verify direct stream settings
   - Check network bandwidth

4. Missing Libraries
   - Verify user permissions
   - Check library sharing settings
   - Refresh library metadata

### Logs to Collect
- App logs
- Jellyfin server logs
- Network traffic (if possible)
- Error messages and stack traces

## Performance Metrics
Monitor and record:
1. Login response time
2. Library load time
3. Media item load time
4. Stream initialization time
5. Memory usage
6. Network bandwidth usage

## Reporting Issues
Include:
1. Server version
2. Test scenario
3. Steps to reproduce
4. Expected vs actual behavior
5. Relevant logs
6. Network configuration
7. Media file details (if applicable)
