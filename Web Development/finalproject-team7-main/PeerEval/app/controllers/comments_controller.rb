class CommentsController < ApplicationController
  # Ensure the user is authenticated 
  before_action :authenticate_user!

  # Handles the creation of a new comment for a submission
  def create
    # Find submission based on the ID and check if the user already commented on this submission
    submission = Submission.find(params[:submission_id])
    if submission.already_commented_by?(current_user)
      # Alert
      flash[:alert] = "You have already commented on this submission."
    else
      # Build a new comment
      comment = submission.comments.build(comment_params.merge(user: current_user))

      # Attempting to save it and flashing alerts accordingly
      if comment.save
        flash[:notice] = "Comment added successfully."
      else
        flash[:alert] = "Failed to add comment."
      end
    end
    redirect_back fallback_location: student_dashboard_path
  end

  private

  def comment_params
    params.require(:comment).permit(:content)
  end
end
