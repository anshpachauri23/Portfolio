class CoursesController < ApplicationController
 
  before_action :set_course, only: [:show, :edit, :update, :destroy]

  # GET /courses
  def index
    @courses = Course.all
  end

  # GET /courses/1
  def show
    @course = Course.find(params[:id])
  end

  # GET /courses/new
  def new
    @course = Course.new
    @students = User.where(role: 'student')
  end

  # POST /courses
  def create
      @course = Course.new(course_params.except(:student_ids))
  
  # Convert student_ids to integers (if present) and assign to `students`
    if params[:course][:student_ids].present?
      student_ids = params[:course][:student_ids].map(&:to_i) # Ensure array of integers
      @course.students = student_ids
      Rails.logger.debug "Processed Student IDs: #{student_ids}"
    end
  
    if @course.save
      redirect_to @course, notice: 'Course was successfully created.'
    else
      render :new
    end
  end

  # GET /courses/1/edit
  def edit
    @course = Course.find(params[:id])
    @course.students = @course.students.map(&:to_i) unless @course.students.nil?
    @students = User.where(role: 'student')
  end

  # PATCH/PUT /courses/1
  def update
    @course = Course.find(params[:id])
    @students = User.where(role: 'student')

    if params[:course][:student_ids].present?
      student_ids = params[:course][:student_ids].map(&:to_i) # Ensure array of integers
      @course.students = student_ids
    end

    if @course.update(course_params.except(:student_ids))
      
      redirect_to @course, notice: 'Course was successfully updated.'
    else
      render :edit
    end
  end

  # DELETE /courses/1
  def destroy
    @course.destroy
    redirect_to courses_url, notice: 'Course was successfully destroyed.'
  end

  private
    # Set course for show, edit, update, destroy
    def set_course
      @course = Course.find(params[:id])
    end

    # Only allow a list of trusted parameters through
    def course_params
      params.require(:course).permit(:name, :semester, :instructor_id, student_ids: [])
    end
end
