class InstructorDashboardController < ApplicationController
  before_action :authenticate_user!
  before_action :ensure_instructor

  def home
    # Get the latest submission for each student and homework
    @course = Course.find(params[:course_id])
    homeworks_for_course = Homework.where(course_id: @course.id).pluck(:id)

  # Filter submissions to include only those that belong to the course
  @submissions = Submission.where(
    id: Submission.select('MAX(id) as id')
                    .group(:student_id, :homework_id)
                    .where(homework_id: homeworks_for_course)
  )

  @homeworks = Homework.where(course_id: @course.id)
  end

  def new_homework
    @homework = Homework.new(course_id: params[:course_id])
  end

  def create_homework
    @homework = Homework.new(homework_params)
    if @homework.save
      flash[:notice] = 'Homework successfully created!'
      redirect_to instructor_dashboard_path(course_id: @homework.course_id)
    else
      flash[:alert] = 'There was an error creating the homework.'
      render :new_homework
    end
  end

  def grade_submission
    @submission = Submission.includes(:comments, :grades).find(params[:submission_id])
    @homework = @submission.homework
    @file = @submission.file # Ensure that the submitted file is accessible
    @comments = @submission.comments
    @grades = @submission.grades
  end
  
  def update_grade
    @submission = Submission.find(params[:submission_id])
    if @submission.update(grade_params)
      flash[:notice] = 'Grade updated successfully!'
      redirect_to instructor_dashboard_path(course_id: Homework.find(@submission.homework_id).course_id)
    else
      flash[:alert] = 'There was an error updating the grade.'
      render :grade_submission
    end
  end

  def view_feedback
    def view_feedback
      @submission = Submission.find(params[:submission_id])
      @comments = Comment.where(submission_id: @submission.id) || []
      @grades = Grade.where(submission_id: @submission.id) || []
    
      @submission_comments = @comments.where(submission_id: @submission.id) || []
      @submission_grades=@grades.where(submission_id: @submission.id) || []
    
      # Build a hash to associate grades with user_id
      @grades_by_user = @grades.index_by(&:user_id)
    end
    
  end

  private

  def ensure_instructor
    redirect_to root_path, alert: 'Access denied!' unless current_user.role == 'instructor'
  end

  def grade_params
    params.require(:submission).permit(:grade, :feedback)
  end

  def homework_params
    params.require(:homework).permit(:title, :description, :due_date, :course_id)
  end
end