# Prerequisites

- Node 14.6.0 or newer
- A c++ compiler

## For Windows

- To install all required build tools, open a PowerShell as Administrator and run `npm install --global windows-build-tools`
- During the build, we will create symlinks. This requires windows to either run in [developer mode](https://docs.microsoft.com/en-us/windows/uwp/get-started/enable-your-device-for-development) or the scripts to be run as administrator.

# Development Build
How to start the development build

1. Have a Camunda BPM Runtime distribution or development server running on port 8080.
2. Install the project with `npm i`
2. Start the development server using `npm start`	
3. The Site will on port 3000. To get to Cockpit, navigate to `/camunda/app/cockpit/default/`.

# Production Build
Please use the maven targets in the root directory to create a distribution.