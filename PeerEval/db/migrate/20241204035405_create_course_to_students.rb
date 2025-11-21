class CreateCourseToStudents < ActiveRecord::Migration[7.2]
  def change
    create_table :course_to_students do |t|
      t.integer :course_id
      t.integer :student_id

      t.timestamps
    end
  end
end
