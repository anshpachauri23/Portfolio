import { useState } from "react";
import { cn } from "@/lib/utils";

const skills = [
  // Programming Languages
  { name: "Python", level: 95, category: "languages" },
  { name: "Java", level: 90, category: "languages" },
  { name: "JavaScript", level: 90, category: "languages" },
  { name: "TypeScript", level: 85, category: "languages" },
  { name: "Go", level: 80, category: "languages" },
  { name: "Ruby", level: 85, category: "languages" },
  { name: "SQL", level: 85, category: "languages" },
  { name: "HTML/CSS", level: 90, category: "languages" },
  { name: "C", level: 80, category: "languages" },

  // Frameworks & Libraries
  { name: "React", level: 90, category: "frameworks" },
  { name: "Next.js", level: 85, category: "frameworks" },
  { name: "Node.js", level: 85, category: "frameworks" },
  { name: "Ruby on Rails", level: 85, category: "frameworks" },
  { name: "Tailwind CSS", level: 90, category: "frameworks" },
  { name: "Bootstrap", level: 80, category: "frameworks" },
  { name: "TensorFlow", level: 85, category: "frameworks" },
  { name: "OpenCV", level: 80, category: "frameworks" },
  { name: "NumPy", level: 90, category: "frameworks" },
  { name: "MATLAB", level: 75, category: "frameworks" },

  // Cloud & AI
  { name: "AWS", level: 90, category: "cloud" },
  { name: "AWS Lambda", level: 85, category: "cloud" },
  { name: "AWS Bedrock", level: 80, category: "cloud" },
  { name: "AWS S3", level: 85, category: "cloud" },
  { name: "AWS RDS", level: 80, category: "cloud" },
  { name: "AWS EC2", level: 75, category: "cloud" },
  { name: "PostgreSQL", level: 85, category: "cloud" },
  { name: "DynamoDB", level: 80, category: "cloud" },
  { name: "SQLite3", level: 80, category: "cloud" },
  { name: "Machine Learning", level: 90, category: "cloud" },
  { name: "Git/GitHub", level: 95, category: "cloud" },
  { name: "Jupyter Notebook", level: 85, category: "cloud" },
  { name: "MATLAB", level: 75, category: "cloud" },
  { name: "Eclipse", level: 80, category: "cloud" },
];

const categories = ["all", "languages", "frameworks", "cloud"];

export const SkillsSection = () => {
  const [activeCategory, setActiveCategory] = useState("all");

  const filteredSkills = skills.filter(
    (skill) => activeCategory === "all" || skill.category === activeCategory
  );
  return (
    <section id="skills" className="py-24 px-4 relative bg-secondary/30">
      <div className="container mx-auto max-w-5xl">
        <h2 className="text-3xl md:text-4xl font-bold mb-12 text-center">
          My <span className="text-primary"> Skills</span>
        </h2>

        <div className="flex flex-wrap justify-center gap-4 mb-12">
          {categories.map((category, key) => (
            <button
              key={key}
              onClick={() => setActiveCategory(category)}
              className={cn(
                "px-5 py-2 rounded-full transition-colors duration-300 capitalize",
                activeCategory === category
                  ? "bg-primary text-primary-foreground"
                  : "bg-secondary/70 text-forefround hover:bd-secondary"
              )}
            >
              {category}
            </button>
          ))}
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredSkills.map((skill, key) => (
            <div
              key={key}
              className="bg-card p-6 rounded-lg shadow-xs card-hover"
            >
              <div className="text-left mb-4">
                <h3 className="font-semibold text-lg"> {skill.name}</h3>
              </div>
              <div className="w-full bg-secondary/50 h-2 rounded-full overflow-hidden">
                <div
                  className="bg-primary h-2 rounded-full origin-left animate-[grow_1.5s_ease-out]"
                  style={{ width: skill.level + "%" }}
                />
              </div>

              <div className="text-right mt-1">
                <span className="text-sm text-muted-foreground">
                  {skill.level}%
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
};
