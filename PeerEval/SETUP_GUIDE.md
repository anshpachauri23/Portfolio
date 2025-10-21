# PeerEval Rails Application - Setup Guide

## 🚀 Quick Start Options

You have several options to run the PeerEval application:

### Option 1: Direct Ruby Setup (Recommended)

1. **Prerequisites**: Ensure you have Ruby 3.2.0 or higher installed
2. **Run the setup script**:
   ```bash
   ./setup_and_run.sh
   ```

### Option 2: Docker Setup (Alternative)

1. **Using Docker Compose**:
   ```bash
   docker-compose up --build
   ```

2. **Using Docker directly**:
   ```bash
   docker build -f Dockerfile.dev -t peereval-dev .
   docker run -p 3000:3000 -v $(pwd):/rails peereval-dev
   ```

### Option 3: Manual Setup

If the automated scripts don't work, follow these manual steps:

1. **Install Ruby 3.2.0+**:
   ```bash
   # Using rbenv (recommended)
   rbenv install 3.2.0
   rbenv local 3.2.0
   
   # Or using rvm
   rvm install 3.2.0
   rvm use 3.2.0
   ```

2. **Install Rails**:
   ```bash
   gem install rails
   ```

3. **Install dependencies**:
   ```bash
   bundle install
   ```

4. **Setup database**:
   ```bash
   rails db:migrate
   ```

5. **Start the server**:
   ```bash
   rails server
   ```

## 🌐 Accessing the Application

Once the server is running:

- **URL**: http://localhost:3000
- **Instructor Sign-up Code**: `PROFESSOR2024`

## 🎯 Application Features

### For Instructors/TAs
- Create and manage courses
- Schedule presentation events
- Add students to courses
- View evaluation analytics
- Assign grades based on feedback

### For Students
- Submit evaluations for peer presentations
- View personal feedback
- Access course materials

## 🛠️ Troubleshooting

### Common Issues

1. **Ruby Version Issues**:
   - Ensure you have Ruby 3.2.0 or higher
   - Use a Ruby version manager (rbenv, rvm)

2. **Bundle Install Fails**:
   - Check Ruby version compatibility
   - Try: `bundle update`

3. **Database Issues**:
   - Ensure SQLite is installed
   - Try: `rails db:reset`

4. **Port Already in Use**:
   - Kill existing Rails processes: `pkill -f rails`
   - Or use a different port: `rails server -p 3001`

### Docker Issues

1. **Docker not running**:
   - Start Docker Desktop
   - Wait for it to fully start

2. **Permission issues**:
   - Ensure Docker has proper permissions
   - Try: `sudo docker-compose up`

## 📱 Application Usage

### First Time Setup

1. **Start the application** using one of the methods above
2. **Navigate to** http://localhost:3000
3. **Sign up as an Instructor** using code: `PROFESSOR2024`
4. **Create a course** and add students
5. **Schedule presentation events**
6. **Students can then submit evaluations**

### Key Features to Test

- ✅ User authentication (sign up, sign in)
- ✅ Course management
- ✅ Event scheduling
- ✅ Student enrollment
- ✅ Evaluation submission
- ✅ Analytics dashboard

## 🔧 Development

### Project Structure
```
PeerEval/
├── app/
│   ├── controllers/     # Rails controllers
│   ├── models/         # Data models
│   ├── views/          # HTML templates
│   └── assets/         # CSS, JS, images
├── config/             # Configuration files
├── db/                 # Database migrations
└── test/               # Test files
```

### Key Technologies
- **Ruby on Rails 7.2.2**
- **SQLite Database**
- **Bootstrap 5.2** for styling
- **Devise** for authentication
- **Hotwire** for dynamic interactions

## 📞 Support

If you encounter issues:

1. Check the troubleshooting section above
2. Ensure all prerequisites are installed
3. Check the Rails logs for error messages
4. Try the Docker approach if Ruby setup fails

---

*Happy coding! 🚀*
