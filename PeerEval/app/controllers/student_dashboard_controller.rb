class StudentDashboardController < ApplicationController
  before_action :authenticate_user!
  before_action :ensure_student

  def home
    # Display available homework for students

    @course = Course.find(params[:course_id])
    @submissions = Submission.where(student_id: current_user.id).group_by(&:homework_id).transform_values(&:last)
    @homeworks = Homework.where(course_id: @course.id)
    
  end
  

  # Show the form to submit homework
  def submit_homework
  
  @homework = Homework.find(params[:homework_id])
  @submission = Submission.new
  # Fetch only the latest submission for the logged-in student and this homework
  @latest_submission = Submission.where(student_id: current_user.id, homework_id: @homework.id).order(created_at: :desc).first
  @comments = @latest_submission&.comments

  end

  

   # Handle the creation of a new homework submission
  def create_submission
    @homework = Homework.find(params[:homework_id])
    @submission = Submission.new(submission_params.merge(student_id: current_user.id, homework: @homework))
    
    if @submission.save
      Rails.logger.info "DEBUG: Submission saved successfully with ID: #{@submission.id}"
      Rails.logger.info "DEBUG: File attached? #{@submission.file.attached?}"
      flash[:notice] = 'Homework submitted successfully!'
      redirect_to submit_homework_path(@homework)
    else
      flash.now[:alert] = 'There was an error submitting your homework.'
      render :submit_homework
    end
  end
  

   # View submissions for a homework, excluding the current student's submissions
  def view_submissions
    @homework = Homework.find_by(id: params[:homework_id])
    Rails.logger.info "Homework: #{@homework.inspect}"
  
    if @homework
      @submissions = Submission.where(
        id: Submission.select('MAX(id) as id')
                       .where(homework_id: @homework.id)
                       .where.not(student_id: current_user.id) # Exclude the logged-in student's submissions
                       .group(:student_id)
      )
      Rails.logger.info "Submissions: #{@submissions.inspect}"
    else
      @submissions = []
      Rails.logger.warn "No homework found for homework_id=#{params[:homework_id]}"
    end
  end
  
  
  
  private
   # Restrict access to students only
  def ensure_student
    redirect_to root_path, alert: 'Access denied!' unless current_user.role == 'student'
  end

  # Permit only allowed attributes for a submission
  def submission_params
    params.require(:submission).permit(:file, :grade, :feedback)
  end
  
end
