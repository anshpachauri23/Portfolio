require "application_system_test_case"

class ClassToStudentsTest < ApplicationSystemTestCase
  setup do
    @class_to_student = class_to_students(:one)
  end

  test "visiting the index" do
    visit class_to_students_url
    assert_selector "h1", text: "Class to students"
  end

  test "should create class to student" do
    visit class_to_students_url
    click_on "New class to student"

    fill_in "Class", with: @class_to_student.class_id
    fill_in "Student", with: @class_to_student.student_id
    click_on "Create Class to student"

    assert_text "Class to student was successfully created"
    click_on "Back"
  end

  test "should update Class to student" do
    visit class_to_student_url(@class_to_student)
    click_on "Edit this class to student", match: :first

    fill_in "Class", with: @class_to_student.class_id
    fill_in "Student", with: @class_to_student.student_id
    click_on "Update Class to student"

    assert_text "Class to student was successfully updated"
    click_on "Back"
  end

  test "should destroy Class to student" do
    visit class_to_student_url(@class_to_student)
    click_on "Destroy this class to student", match: :first

    assert_text "Class to student was successfully destroyed"
  end
end
