# Smart Campus Resource Allocation System
The **Smart Campus Resource Allocation System** is a modern, locally-hosted web application designed to efficiently manage and allocate campus resources such as classrooms, laboratories, seminar halls, and projectors.
=======
The *Smart Campus Resource Allocation System* is a Java-powered desktop application with an intuitive Graphical User Interface (GUI), purpose-built to manage and allocate campus resources — including classrooms, laboratories, seminar halls, and projectors — with precision and ease.
>>>>>>> a1394a14e60d6fc754d1e4ace00e199414a1fda2

Inadequate scheduling in academic institutions often leads to booking conflicts and underutilized resources. This application addresses that gap by delivering a centralized, database-driven platform capable of dynamically managing users, resources, and bookings while maintaining a fully conflict-free scheduling environment

## Features

* **Dynamic Resource Booking:** Enter a date and time slot, and the system will automatically query the database to show you only the resources that are currently available.
* **View Bookings:** A comprehensive dashboard to view all past and upcoming bookings, including their approval status.
* **Manage Data:** Built-in forms to easily add new **Departments**, **Users**, and **Resources** to the campus network.
* **Approval Workflow (Password Protected):** A secure "Approve Bookings" tab allowing administrators to approve or reject pending resource requests. Accessing this tab requires the admin password (default: `root`).
* **Usage Logging:** Track actual resource usage, including actual start times, end times, and duration, through the "Usage Logs" tab.
* **Modern Web Interface:** A sleek, fully revamped Single Page Application (SPA) frontend built with Vanilla HTML, CSS, and JS. Features a premium design with glassmorphism, smooth animations, and toast notifications.
* **Dark/Light Theme Toggle:** Instantly switch the application between a modern Dark Mode (pitch black) and a clean Light Mode with a single click, without reloading the page.

## Technologies Used

* **Backend:** Java 21, Spring Boot Web, Spring Boot REST Controllers
* **Frontend:** Vanilla HTML5, CSS3, Vanilla JavaScript (Fetch API)
* **Database:** MySQL

## Dependencies Required

To run this application, you must have the following installed on your system:

1. **Java Development Kit (JDK):** Version 21 or higher.
2. **Maven:** Installed and added to your system PATH.
3. **MySQL Server:** Running locally on port `3306` with the username and password both set to `root`.

## Step-by-Step Guide: How to Run

Follow these instructions to start the web application:

### Step 1: Database Setup
1. Ensure your local **MySQL Server** is running.
2. If you haven't already, create a database named `smartcampus`.
3. If you are starting fresh, you can execute the contents of `schema.sql` inside your MySQL environment to automatically generate all the necessary tables and populate them with sample data.

### Step 2: Open Terminal
Open your terminal (Command Prompt, PowerShell, or bash) and navigate to the root directory of the cloned project:
```bash
cd path/to/project-directory
```

### Step 3: Run with Maven
You can start the Spring Boot web server using the Maven Spring Boot plugin. Run the following command:
```bash
mvn clean spring-boot:run
```

### Step 4: Access the Application
Once the application starts successfully and you see the `Tomcat started on port 8080` message in your console, open your favorite web browser and navigate to:

**[http://localhost:8080](http://localhost:8080)**

The modern dashboard will instantly load on your screen. Use the sidebar to explore the different features!
