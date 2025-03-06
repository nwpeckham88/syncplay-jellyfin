# Manual Testing Guide for Jellyfin Integration

## Debug Test Runner
For manual testing with a real Jellyfin server, the app includes a test runner interface in debug builds.

### How to Access
1. Build and install the debug variant of the app
2. Look for "Jellyfin Test Runner" in your app launcher
3. Or launch via adb:
```bash
adb shell am start -n com.yuroyami.syncplay.debug/com.yuroyami.syncplay.JellyfinTestRunner
```

### Features
- Input fields for server URL, username, and password
- Individual test buttons for specific features
- Full test suite execution
- Live test output display
- Performance metrics tracking

### Running Tests
1. Enter your Jellyfin server details:
   - Server URL (e.g., http://192.168.1.100:8096)
   - Username
   - Password

2. Choose a test to run:
   - "Test Authentication": Verifies server connection and credentials
   - "Test Library Access": Lists available media libraries
   - "Run All Tests": Executes the complete test suite

3. Monitor the output area for:
   - Test progress and results
   - API call metrics
   - Error messages if any

### Test Cases

#### Authentication
- Valid credentials should show success message
- Invalid credentials should show appropriate error
- Server URL format should be validated
- Check API token is received

#### Library Access
- All media libraries should be listed
- Library types should be correctly identified
- Thumbnails URLs should be properly constructed
- Permissions should be respected

#### Media Browsing
- Items within libraries should load
- Metadata should be complete
- Thumbnails should be accessible
- Pagination should work (if implemented)

#### Streaming
- Stream URLs should be generated
- URLs should include valid API tokens
- Direct stream options should be available
- Check URL format matches server configuration

### Performance Metrics
The test runner tracks:
1. API call durations
2. Success/failure rates
3. Average response times
4. Network efficiency

### Common Issues
1. Server Connection:
   - Check server is running and accessible
   - Verify network connectivity
   - Confirm port is open and correct

2. Authentication:
   - Verify user exists and is active
   - Check password is correct
   - Confirm user has necessary permissions

3. Media Access:
   - Verify libraries exist
   - Check media files are present
   - Confirm user has library access
   - Validate file permissions

### Testing Environment Setup
1. Local Testing:
   ```bash
   docker run -d \
     --name jellyfin \
     -p 8096:8096 \
     -v /path/to/media:/media \
     jellyfin/jellyfin:latest
   ```

2. Test Account Setup:
   - Create dedicated test user
   - Set appropriate permissions
   - Add test media content

3. Network Configuration:
   - Note server IP address
   - Configure firewall if needed
   - Use HTTP for initial testing

### Reporting Issues
When reporting problems, include:
1. Test output
2. Server version
3. Network configuration
4. Steps to reproduce
5. Performance metrics
6. Error messages
7. Media details if relevant

### Best Practices
1. Start with authentication tests
2. Verify library access before media tests
3. Test with various media types
4. Check error handling
5. Monitor performance metrics
6. Document any issues found
7. Test network edge cases

### Troubleshooting
1. Authentication Failed:
   - Check server URL format
   - Verify credentials
   - Check server logs

2. Missing Libraries:
   - Verify user permissions
   - Check library configuration
   - Confirm media paths

3. Stream Issues:
   - Verify media exists
   - Check streaming settings
   - Confirm network access

4. Performance Problems:
   - Monitor API metrics
   - Check network conditions
   - Verify server resources

### Next Steps
After successful testing:
1. Document any discovered issues
2. Note performance metrics
3. Suggest improvements
4. Test edge cases
5. Verify error handling
6. Check resource usage
7. Consider scalability
