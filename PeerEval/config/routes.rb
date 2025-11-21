Rails.application.routes.draw do
  # Devise routes for user authentication
  devise_for :users, controllers: { registrations: 'users/registrations' }

  # Root route for unauthenticated users
  root to: 'welcome#index'
  resources :courses do
    resources :course_to_students, only: [:new, :create]
  end

  # Default route for logged-in users based on role
  authenticated :user, ->(u) { u.role == 'student' } do
    root to: 'courses#index', as: :student_authenticated_root
  end

  authenticated :user, ->(u) { u.role == 'instructor' } do
    root to: 'courses#index', as: :instructor_authenticated_root
  end

  unauthenticated do
    root to: 'welcome#index', as: :unauthenticated_root
  end
  resources :submissions, only: [] do
    resources :comments, only: [:create]
    resources :grades, only: [:create]
  end

  # Student Dashboard Routes
  get 'student_dashboard', to: 'student_dashboard#home', as: 'student_dashboard'
  get 'student_dashboard/homeworks/:homework_id/submit', to: 'student_dashboard#submit_homework', as: 'submit_homework'
  post 'student_dashboard/homeworks/:homework_id/submissions', to: 'student_dashboard#create_submission', as: 'create_submission'
  
  # New routes for viewing other students' submissions
  get 'student_dashboard/homeworks/:homework_id/view_submissions', to: 'student_dashboard#view_submissions', as: 'view_submissions_student_dashboard'
  get 'student_dashboard/submissions/:id', to: 'student_dashboard#view_submission', as: 'submission_student_dashboard'

  # Instructor Dashboard Routes
  get 'instructor_dashboard', to: 'instructor_dashboard#home', as: 'instructor_dashboard'
  get 'instructor_dashboard/submissions/:submission_id/grade', to: 'instructor_dashboard#grade_submission', as: 'grade_submission'
  patch 'instructor_dashboard/submissions/:submission_id', to: 'instructor_dashboard#update_grade', as: 'update_grade'
  get 'instructor_dashboard/homeworks/new', to: 'instructor_dashboard#new_homework', as: 'new_homework'
  post 'instructor_dashboard/homeworks', to: 'instructor_dashboard#create_homework', as: 'create_homework'
  get 'instructor_dashboard/submissions/:submission_id/feedback', to: 'instructor_dashboard#view_feedback', as: 'feedback'


  # Health status check route for uptime monitoring
  get "up" => "rails/health#show", as: :rails_health_check

  


  # Render dynamic PWA files from app/views/pwa/*
  get "service-worker" => "rails/pwa#service_worker", as: :pwa_service_worker
  get "manifest" => "rails/pwa#manifest", as: :pwa_manifest
end
