# RSS News Feed demo project that uses `Java 19 Virtual Threads`

## Overview
This is the demo app to demo the `Java 19 Virtual Threads` power. The service returns RSS feed 
for selected source. The source can be added by any registered user, though the source name is globally 
unique. The system allows the user to import RSS file as a batch operation.

## Technology stack
* Java 19 with `Virtual Threads` enabled
* PostgreSQL
* Socket API to handle requests (for simplicity)
