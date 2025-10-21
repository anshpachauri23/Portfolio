require "application_system_test_case"

class CourseToStudentsTest < ApplicationSystemTestCase
  setup do
    @course_to_student = course_to_students(:one)
  end

  test "visiting the index" do
    visit course_to_students_url
    assert_selector "h1", text: "Course to students"
  end

  test "should create course to student" do
    visit course_to_students_url
    click_on "New course to student"

    fill_in "Course", with: @course_to_student.course_id
    fill_in "Student", with: @course_to_student.student_id
    click_on "Create Course to student"

    assert_text "Course to student was successfully created"
    click_on "Back"
  end

  test "should update Course to student" do
    visit course_to_student_url(@course_to_student)
    click_on "Edit this course to student", match: :first

    fill_in "Course", with: @course_to_student.course_id
    fill_in "Student", with: @course_to_student.student_id
    click_on "Update Course to student"

    assert_text "Course to student was successfully updated"
    click_on "Back"
  end

  test "should destroy Course to student" do
    visit course_to_student_url(@course_to_student)
    click_on "Destroy this course to student", match: :first

    assert_text "Course to student was successfully destroyed"
  end
end
