pipeline {{
    agent any

    stages {{
        stage('Run selected tests') {{
            steps {{
                script {{

                    def jobs = [:]

                    if (params.RUN_API) {{
                        jobs["API tests"] = {{
                            build job: "api-tests"
                        }}
                    }}

                    if (params.RUN_UI) {{
                        jobs["UI tests"] = {{
                            build job: "ui-test"
                        }}
                    }}

                    if (params.RUN_MOBILE) {{
                        jobs["Mobile tests"] = {{
                            build job: "mobile-tests"
                        }}
                    }}

                    if (jobs.isEmpty()) {{
                        error("No jobs selected")
                    }}

                    parallel jobs
                }}
            }}
        }}
    }}
}}