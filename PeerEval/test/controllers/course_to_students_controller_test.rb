require "test_helper"

class CourseToStudentsControllerTest < ActionDispatch::IntegrationTest
  setup do
    @course_to_student = course_to_students(:one)
  end

  test "should get index" do
    get course_to_students_url
    assert_response :success
  end

  test "should get new" do
    get new_course_to_student_url
    assert_response :success
  end

  test "should create course_to_student" do
    assert_difference("CourseToStudent.count") do
      post course_to_students_url, params: { course_to_student: { course_id: @course_to_student.course_id, student_id: @course_to_student.student_id } }
    end

    assert_redirected_to course_to_student_url(CourseToStudent.last)
  end

  test "should show course_to_student" do
    get course_to_student_url(@course_to_student)
    assert_response :success
  end

  test "should get edit" do
    get edit_course_to_student_url(@course_to_student)
    assert_response :success
  end

  test "should update course_to_student" do
    patch course_to_student_url(@course_to_student), params: { course_to_student: { course_id: @course_to_student.course_id, student_id: @course_to_student.student_id } }
    assert_redirected_to course_to_student_url(@course_to_student)
  end

  test "should destroy course_to_student" do
    assert_difference("CourseToStudent.count", -1) do
      delete course_to_student_url(@course_to_student)
    end

    assert_redirected_to course_to_students_url
  end
end
