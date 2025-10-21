class GradesController < ApplicationController
  # Making sure the user is authenticating before accessing this controller
  before_action :authenticate_user!

  # Handles the creation of a new grade for a submission
  def create
    # Getting grade based on ID and checking if it is already graded
    submission = Submission.find(params[:submission_id])
    # Alert
    if submission.already_graded_by?(current_user)
      flash[:alert] = "You have already graded this submission."
    else
      # Attempting to save it and flashes alerts accordingly
      grade = submission.grades.build(grade_params.merge(user: current_user))
      if grade.save
        flash[:notice] = "Grade added successfully."
      else
        flash[:alert] = "Failed to add grade."
      end
    end
    redirect_back fallback_location: student_dashboard_path
  end

  private

  def grade_params
    params.require(:grade).permit(:grade)
  end
end
