class ApplicationController < ActionController::Base
  allow_browser versions: :modern

  # Redirect logged-in users from the welcome page to the dashboard
  before_action :redirect_authenticated_user, if: :user_signed_in?

  # Make the method accessible in views
  before_action :configure_permitted_parameters, if: :devise_controller?

  protected

  def configure_permitted_parameters
    # Permit additional parameters for sign-up and account update
    devise_parameter_sanitizer.permit(:sign_up, keys: [:fname, :lname, :sign_up_code])
    devise_parameter_sanitizer.permit(:account_update, keys: [:fname, :lname])
  end

  private

  # Redirect authenticated users if they visit the welcome page
  def redirect_authenticated_user
    # Only redirect if the user is on the root path
    if request.path == root_path
      if current_user.role == 'student'
        redirect_to courses_path   
      elsif current_user.role == 'instructor'
        redirect_to courses_path   
      end
    end
  end
end
