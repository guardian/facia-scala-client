import type { GuStackProps } from '@guardian/cdk/lib/constructs/core';
import { GuStack } from '@guardian/cdk/lib/constructs/core';
import type { App } from 'aws-cdk-lib';
import { CfnOutput } from 'aws-cdk-lib';
import { FederatedPrincipal, PolicyStatement, Role } from 'aws-cdk-lib/aws-iam';

export class FaciaScalaClientTesting extends GuStack {
    constructor(scope: App, id: string, props: GuStackProps) {
        super(scope, id, props);
        const fapiBucketArn = 'arn:aws:s3:::facia-tool-store';

        const role = new Role(this, 'GithubActionsRole', {
            assumedBy: new FederatedPrincipal(
                `arn:aws:iam::${this.account}:oidc-provider/token.actions.githubusercontent.com`,
                {
                    StringEquals: {
                        'token.actions.githubusercontent.com:aud': 'sts.amazonaws.com',
                    },
                    StringLike: {
                        // guardian org id (164318) + facia-scala-client repo id (20723151)
                        'token.actions.githubusercontent.com:sub':
                            'repo:guardian@164318/facia-scala-client@20723151:*',
                    },
                },
                'sts:AssumeRoleWithWebIdentity',
            ),
        });

        role.addToPolicy(
            new PolicyStatement({
                actions: [
                    's3:GetObject', // required by FAPI to download files
                    's3:ListBucket', // avoids S3 AccessDenied when FAPI requests nonexistent objects
                ],
                resources: [
                    `${fapiBucketArn}/DEV/*`, // object resource for s3:GetObject
                    fapiBucketArn, // bucket resource for s3:ListBucket
                ],
            }),
        );

        new CfnOutput(this, 'GithubActionsRole-Arn', { value: role.roleArn });
    }
}