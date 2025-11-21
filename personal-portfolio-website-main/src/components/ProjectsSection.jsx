import { ArrowRight, ExternalLink, Github } from "lucide-react";

const projects = [
  {
    id: 1,
    title: "RecipeShare - Social Recipe Network",
    description: "Built a full-stack social media platform for recipe sharing with 15+ features including authentication, follow system, real-time notifications, commenting, liking, and comprehensive profile management. Engineered scalable backend REST APIs using Go and PostgreSQL, implemented JWT authentication, integrated AWS S3 for media storage, and designed responsive UI with Next.js and Tailwind CSS supporting 100+ concurrent users with optimized database query performance.",
    image: "/projects/project1.png",
    tags: ["Next.js", "TypeScript", "Go", "PostgreSQL", "AWS S3", "AWS RDS", "Tailwind CSS"],
    demoUrl: "https://recipe-social-media-personal.vercel.app/auth/login",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 2,
    title: "E-commerce Platform",
    description: "Architected a scalable serverless backend (Node.js, AWS Lambda) handling 500+ concurrent users with sub-200ms response times. Built a complete buying workflow (checkout, payment details, confirmation) and optimized UI, reducing cart abandonment by 25%.",
    image: "/projects/project2.png",
    tags: ["React.js", "Node.js", "AWS Lambda", "DynamoDB", "API Gateway"],
    demoUrl: "https://jear-ecommerce-cse-5234.vercel.app/",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 3,
    title: "Real-Time Market Sentiment Analysis Engine",
    description: "Engineered a distributed streaming architecture to ingest high-velocity financial news feeds using Apache Kafka and Docker containerization; built a sentiment analysis worker using Vector Search (RAG) and FAISS to correlate live news events with historical market context, delivering sub-second bullish/bearish trend indicators to a React dashboard for immediate decision support.",
    image: "/projects/project4.png",
    tags: ["Python", "Docker", "Kafka", "FAISS", "React", "RAG", "Vector Search"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 4,
    title: "Core Language Interpreter",
    description: "Designed and implemented a production-grade interpreter with lexical analysis, parsing, and recursive-descent execution processing 1,000+ lines/sec; incorporated robust reference-counting garbage collection and syntax checks ensuring 99.9% parsing accuracy for reliable execution.",
    image: null,
    tags: ["Java", "Compiler Design", "Memory Management", "Garbage Collection"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
  {
    id: 5,
    title: "PeerEval - Academic Assessment Platform",
    description: "Developed a full-stack CRUD platform for 200+ students, reducing submission errors by 35%, improving SQL query speeds by 2s/request, and communicating technical progress and performance in presentations to peers and instructors throughout the development cycle.",
    image: "/projects/project3.png",
    tags: ["Ruby on Rails 7.2", "SQLite3", "Bootstrap 5", "Full-Stack"],
    demoUrl: "#",
    githubUrl: "https://github.com/anshpachauri23/Portfolio",
  },
];

export const ProjectsSection = () => {
  return (
    <section id="projects" className="py-24 px-4 relative">
      <div className="container mx-auto max-w-5xl">
        <h2 className="text-3xl md:text-4xl font-bold mb-4 text-center">
          {" "}
          Featured <span className="text-primary"> Projects </span>
        </h2>

        <p className="text-center text-muted-foreground mb-12 max-w-2xl mx-auto">
          Here are some of my recent projects. Each project was carefully
          crafted with attention to detail, performance, and user experience.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {projects.map((project, key) => (
            <div
              key={key}
              className="group bg-card rounded-lg overflow-hidden shadow-xs card-hover"
            >
              {project.image && (
                <div className="h-48 overflow-hidden">
                  <img
                    src={project.image}
                    alt={project.title}
                    className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-110"
                  />
                </div>
              )}

              <div className="p-6">
                <h3 className="text-xl font-semibold mb-1"> {project.title}</h3>
                <p className="text-muted-foreground text-sm mb-4">
                  {project.description}
                </p>
                
                <div className="flex flex-wrap gap-2 mb-4">
                  {project.tags.map((tag) => (
                    <span className="px-2 py-1 text-xs font-medium border rounded-full bg-secondary text-secondary-foreground">
                      {tag}
                    </span>
                  ))}
                </div>
                <div className="flex justify-between items-center">
                  <div className="flex space-x-3">
                    <a
                      href={project.demoUrl}
                      target="_blank"
                      className="text-foreground/80 hover:text-primary transition-colors duration-300"
                    >
                      <ExternalLink size={20} />
                    </a>
                    <a
                      href={project.githubUrl}
                      target="_blank"
                      className="text-foreground/80 hover:text-primary transition-colors duration-300"
                    >
                      <Github size={20} />
                    </a>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        <div className="text-center mt-12">
          <a
            className="cosmic-button w-fit flex items-center mx-auto gap-2"
            target="_blank"
            href="https://github.com/anshpachauri23/Portfolio"
          >
            Check My Github <ArrowRight size={16} />
          </a>
        </div>
      </div>
    </section>
  );
};
