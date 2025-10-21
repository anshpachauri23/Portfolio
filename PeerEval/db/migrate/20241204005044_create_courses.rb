class CreateCourses < ActiveRecord::Migration[7.2]
  def change
    create_table :courses do |t|
      t.string :name
      t.string :semester
      t.integer :instructor_id

      t.timestamps
    end
  end
end
