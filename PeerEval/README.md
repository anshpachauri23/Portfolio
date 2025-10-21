# PeerEval

## Overview

- A tool to streamline the evaluation process for classroom presentations by collecting and analyzing audience feedback.
- Designed to help instructors assign fair grades and provide constructive criticism to presenters.

## About

- **Student Benefits**

  - Easily submit feedback and scores for classmates’ presentations.
  - Access constructive feedback to improve presentation skills over time.

- **Instructor Benefits**

  - Manage presentation events and collect evaluations seamlessly.
  - Gain insights into performance and ensure fair grading.
  - Quickly identify areas of improvement based on audience feedback.

## Users

- **Instructor/TA**

  - Create, read, update, and delete presentation events.
  - View and analyze audience evaluations to assign grades.

- **Student**

  - Submit evaluations (scores and comments) for presentations.
  - View personal feedback provided by the audience.

## Features

- **User Authentication**

  - Secure login system to ensure only authorized users access the application.

- **Course Management**

  - Allows creation of multiple courses for an instructor/TA and adding students to it

- **Homework Management**

  - Allows instructors to create and manage presentation homeworks across the semester.

- **Audience Feedback Collection**

  - Enables students to provide scores and comments for multiple presentations.

- **Administrative Interface**

  - Provides a comprehensive view of evaluations for instructors to make grading decisions.

- **Presenter Insights**

  - Facilitates access for presenters to review their audience feedback for improvement.

## Setup

To set up the server on your own machine, ensure the necessary dependencies are installed. Once you’ve downloaded the repository, navigate to the project directory and run:

```
$ cd PeerEval
```

```
$ rails db:migrate
```

Then, to start the server:

```
$ rails s
```

The server will now be running on your local machine. To view it, go to: `localhost:3000`

In order to sign up as an Instructor, you must use the code 'PROFESSOR2024' while creating a new account.
