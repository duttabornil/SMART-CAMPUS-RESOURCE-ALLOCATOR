-- DDL Commands
CREATE TABLE IF NOT EXISTS Department (
    Department_ID INT PRIMARY KEY,
    Department_Name VARCHAR(100) NOT NULL,
    Block VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS User (
    User_ID INT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE,
    Role ENUM('Student','Faculty','Admin') NOT NULL,
    Department_ID INT,
    FOREIGN KEY (Department_ID) REFERENCES Department(Department_ID)
);

CREATE TABLE IF NOT EXISTS Resource (
    Resource_ID INT PRIMARY KEY,
    Resource_Name VARCHAR(100) NOT NULL,
    Resource_Type VARCHAR(50),
    Location VARCHAR(100),
    Capacity INT,
    Status ENUM('Available','Maintenance') DEFAULT 'Available',
    Department_ID INT,
    FOREIGN KEY (Department_ID) REFERENCES Department(Department_ID)
);

CREATE TABLE IF NOT EXISTS Booking (
    Booking_ID INT PRIMARY KEY,
    Booking_Date DATE,
    Start_Time TIME,
    End_Time TIME,
    Purpose VARCHAR(255),
    Status ENUM('Pending','Approved','Rejected') DEFAULT 'Pending',
    User_ID INT,
    Resource_ID INT,
    FOREIGN KEY (User_ID) REFERENCES User(User_ID),
    FOREIGN KEY (Resource_ID) REFERENCES Resource(Resource_ID)
);

CREATE TABLE IF NOT EXISTS Booking_Slot (
    Booking_ID INT,
    Slot_No INT,
    Slot_Start_Time TIME,
    Slot_End_Time TIME,
    PRIMARY KEY (Booking_ID, Slot_No),
    FOREIGN KEY (Booking_ID) REFERENCES Booking(Booking_ID) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS Usage_Log (
    Log_ID INT PRIMARY KEY,
    Booking_ID INT UNIQUE,
    Actual_Start_Time TIME,
    Actual_End_Time TIME,
    Usage_Duration INT,
    FOREIGN KEY (Booking_ID) REFERENCES Booking(Booking_ID)
);

-- DML Commands (Sample Data)
INSERT IGNORE INTO Department (Department_ID, Department_Name, Block) VALUES
(1, 'Computer Science', 'Block A'),
(2, 'Electronics', 'Block B'),
(3, 'Mechanical', 'Block C');

INSERT IGNORE INTO User (User_ID, Name, Email, Role, Department_ID) VALUES
(101, 'Riya Sharma', 'riya@gmail.com', 'Student', 1),
(102, 'Aman Verma', 'aman@gmail.com', 'Student', 1),
(103, 'Dr. Sen', 'sen@gmail.com', 'Faculty', 2),
(104, 'Admin Raj', 'admin@gmail.com', 'Admin', 1);

INSERT IGNORE INTO Resource (Resource_ID, Resource_Name, Resource_Type, Location, Capacity, Status, Department_ID) VALUES
(201, 'Lab 1', 'Computer Lab', '1st Floor', 40, 'Available', 1),
(202, 'Seminar Hall', 'Hall', 'Ground Floor', 150, 'Available', 1),
(203, 'Projector A', 'Equipment', 'Store Room', 1, 'Available', 2);

INSERT IGNORE INTO Booking (Booking_ID, Booking_Date, Start_Time, End_Time, Purpose, Status, User_ID, Resource_ID) VALUES
(301, '2026-02-12', '10:00:00', '12:00:00', 'Project Discussion', 'Approved', 101, 201),
(302, '2026-02-13', '14:00:00', '16:00:00', 'Workshop', 'Pending', 102, 202);

INSERT IGNORE INTO Booking_Slot (Booking_ID, Slot_No, Slot_Start_Time, Slot_End_Time) VALUES
(301, 1, '10:00:00', '11:00:00'),
(301, 2, '11:00:00', '12:00:00'),
(302, 1, '14:00:00', '15:00:00'),
(302, 2, '15:00:00', '16:00:00');

INSERT IGNORE INTO Usage_Log (Log_ID, Booking_ID, Actual_Start_Time, Actual_End_Time, Usage_Duration) VALUES
(401, 301, '10:05:00', '11:55:00', 110),
(402, 302, '14:05:00', '15:55:00', 110);
